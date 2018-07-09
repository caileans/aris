package edu.rpi.aris.assign.client;

import edu.rpi.aris.assign.ArisClientModule;
import edu.rpi.aris.assign.ArisModuleException;
import edu.rpi.aris.assign.LibAssign;
import edu.rpi.aris.assign.client.gui.Config;
import edu.rpi.aris.assign.spi.ArisModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;


public class ClientModuleService {

    private static final Logger logger = LogManager.getLogger(ClientModuleService.class);
    private static ClientModuleService service = new ClientModuleService();
    private ServiceLoader<ArisModule> loader;
    private HashMap<String, ArisModule> services = new HashMap<>();
    private ArrayList<String> moduleNames = new ArrayList<>();

    private ClientModuleService() {
        try {
            init();
        } catch (ArisModuleException e) {
            LibAssign.getInstance().showExceptionError(Thread.currentThread(), e, true);
        }
    }

    private static void addJarsFromDir(File dir, HashSet<URL> jars) throws MalformedURLException {
        if (dir.exists() && dir.isDirectory()) {
            File[] moduleJars = dir.listFiles(pathname -> !pathname.isDirectory() && pathname.getName().toLowerCase().endsWith(".jar"));
            if (moduleJars != null) {
                for (File f : moduleJars) {
                    jars.add(f.toURI().toURL());
                }
            }
        }
    }

    public static ClientModuleService getService() {
        return service;
    }

    private synchronized void init() throws ArisModuleException {
        logger.info("Initializing ServiceLoader");
        HashSet<URL> jars = new HashSet<>();
        try {
            addJarsFromDir(Config.CLIENT_MODULES_DIR, jars);
            addJarsFromDir(Config.CLIENT_MODULE_LIBS_DIR, jars);
            URL[] urls = new URL[jars.size()];
            jars.toArray(urls);
            URLClassLoader classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
            loader = ServiceLoader.load(ArisModule.class, classLoader);
        } catch (MalformedURLException e) {
            throw new ArisModuleException("Failed to initialize ClassLoader", e);
        }
        services.clear();
        moduleNames.clear();
        logger.info("Loading client modules");
        for (ArisModule s : loader) {
            logger.info("Found module \"" + s.getModuleName() + "\"");
            ArisClientModule m = s.getClientModule();
            if (m == null) {
                logger.warn("Module \"" + s.getModuleName() + "\" did not supply a client module. Skipping");
                continue;
            }
            if (services.put(s.getModuleName(), s) != null) {
                logger.fatal("Multiple modules have been found using the name \"" + s.getModuleName() + "\"");
                logger.fatal("Either remove the extra modules or rename them");
                throw new ArisModuleException("Multiple modules detected with the same name + \"" + s.getModuleName() + "\"");
            }
        }
        moduleNames.addAll(services.keySet());
        Collections.sort(moduleNames);
    }

    public synchronized void reloadModules() throws ArisModuleException {
        logger.info("Client module reload requested");
        loader.reload();
        init();
    }

    public synchronized ArisModule getModule(String moduleName) {
        return services.get(moduleName);
    }

    public synchronized ArisClientModule getClientModule(String moduleName) {
        try {
            return getModule(moduleName).getClientModule();
        } catch (ArisModuleException e) {
            LibAssign.getInstance().showExceptionError(Thread.currentThread(), e, false);
            return null;
        }
    }

    public synchronized List<String> moduleNames() {
        return moduleNames;
    }

}