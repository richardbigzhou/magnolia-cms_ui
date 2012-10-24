This is a patched version of Vaadin's VScrollTable we currently need to maintain ourselves due to some limitations regarding extensibility
in the original class and which Vaadin cannot handle at the moment (TODO add Vaadin tickets raised by Magnolia about this, if any).
In order to ease the maintainance process we use a Maven-based process (see more in the build section of this project's POM) where we
basically apply our own patch (see src/main/patches/vscrolltable.txt) to the class's original sources, then rename it in order to

1) avoid troubles with the original class as to which one the Java classloader will pick.
2) make clear that we're using a patched version of VScrollTable.

TROUBLESHOOTING INFO FOR MAGNOLIA DEVS
--------------------------------------
If you have troubles applying a freshly generated patch with mvn clean install

 then

 - run maven in debug mode to get more information about the cause of failure. Two common error messages are
 -- patch **** malformed patch at line blah
 -- Hunk #1 FAILED at 38
 - ensure the path in the generated patch is correct, namely com/vaadin/terminal/gwt/client/ui/VScrollTable.java
 - ensure no spurious characters have crept into the patch and that line endings are the same on both files.
 - git diff -p sometimes seems to generate an invalid patch. Use diff -wu originalFile patchedFile > my-patch.txt
 - avoid copy/paste b/c you might end up with a malformed patch error due to a missing space in front of each line except for symbols "+", "-" and "@@"


