package com.faithlife.lint

import com.intellij.psi.PsiClass

// https://developer.android.com/reference/packages?hl=en
private val FRAMEWORK_PACKAGE_PREFIXES: Set<String> = hashSetOf("android.", "java.", "javax.")

// todo(jzb): is there a better way to do this? This should work most of the time, but isn't
//  resistant to bad practices were a person to declare their own package with these prefixes
val PsiClass.isAndroidFrameworkType: Boolean
    get() = qualifiedName != null && qualifiedName in FRAMEWORK_PACKAGE_PREFIXES
