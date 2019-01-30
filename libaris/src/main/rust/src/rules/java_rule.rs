use super::*;

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_edu_rpi_aris_rules_RustRule_fromRule(env: JNIEnv, _: JObject, rule: JObject) -> jobject {
    (|| -> jni::errors::Result<jstring> {
        let cls = env.call_method(rule, "getClass", "()Ljava/lang/Class;", &[])?.l()?;
        let classname = String::from(env.get_string(JString::from(env.call_method(cls, "getName", "()Ljava/lang/String;", &[])?.l()?))?);
        println!("RustRule.fromRule, rule class: {:?}", classname);
        if classname != "edu.rpi.aris.rules.RuleList" {
            return Err(jni::errors::Error::from_kind(jni::errors::ErrorKind::Msg(format!("RustRule::fromRule: unknown class {}", classname))));
        }

        let name = String::from(env.get_string(JString::from(env.call_method(rule, "name", "()Ljava/lang/String;", &[])?.l()?))?);
        println!("RustRule.fromRule, rule enum name: {:?}", name);
        let rule = match &*name {
            "CONJUNCTION" => RuleM::AndIntro,
            _ => { return Err(jni::errors::Error::from_kind(jni::errors::ErrorKind::Msg(format!("RustRule::fromRule: unknown enum name {}", name)))) },
        };
        let boxed_rule = Box::into_raw(Box::new(rule)); // prevent boxed_rule from being freed, since it's to be referenced through the java heap

        let jrule = env.new_object("edu/rpi/aris/rules/RustRule", "(J)V", &[JValue::from(boxed_rule as jni::sys::jlong)]);
        println!("RustRule.fromRule, boxed_rule: {:?}, jrule: {:?}", boxed_rule, jrule);
        Ok(jrule?.into_inner())
    })().unwrap_or(std::ptr::null_mut())
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_edu_rpi_aris_rules_RustRule_toString(env: JNIEnv, obj: JObject) -> jstring {
    (|| -> jni::errors::Result<jstring> {
        let ptr: jni::sys::jlong = env.get_field(obj, "pointerToRustHeap", "J")?.j()?;
        let rule: &Rule = unsafe { &*(ptr as *mut Rule) };
        Ok(env.new_string(format!("{:?}", rule))?.into_inner())
    })().unwrap_or(std::ptr::null_mut())
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_edu_rpi_aris_rules_RustRule_requiredPremises(env: JNIEnv, obj: JObject) -> jni::sys::jlong {
    (|| -> jni::errors::Result<_> {
        let ptr: jni::sys::jlong = env.get_field(obj, "pointerToRustHeap", "J")?.j()?;
        let rule: &Rule = unsafe { &*(ptr as *mut Rule) };
        Ok(rule.num_deps().unwrap_or(1) as _) // it looks like the java version represents generalizable premises as 1 premise, with the flag indicating >= instead of ==
    })().unwrap()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_edu_rpi_aris_rules_RustRule_canGeneralizePremises(env: JNIEnv, obj: JObject) -> jni::sys::jboolean {
    (|| -> jni::errors::Result<_> {
        let ptr: jni::sys::jlong = env.get_field(obj, "pointerToRustHeap", "J")?.j()?;
        let rule: &Rule = unsafe { &*(ptr as *mut Rule) };
        Ok(if rule.num_deps().is_none() { 1 } else { 0 })
    })().unwrap()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_edu_rpi_aris_rules_RustRule_subProofPremises(env: JNIEnv, obj: JObject) -> jni::sys::jlong {
    (|| -> jni::errors::Result<_> {
        let ptr: jni::sys::jlong = env.get_field(obj, "pointerToRustHeap", "J")?.j()?;
        let rule: &Rule = unsafe { &*(ptr as *mut Rule) };
        Ok(rule.num_subdeps().unwrap_or(0) as _)
    })().unwrap()
}