This package contains patched versions of Table/TreeTable widgets and connectors, as well as a patched DragAndDropWrapper.
These classes are automatically generated during the maven build, by applying patches located in src/main/patches on top of the corresponding original Vaadin source files.

When changes are required, or when upgrading Vaading, please proceed as follows (read through before starting):
- Create the patched version of the desired class (either manually or by running the maven patch upfront)
    - You may also copy the original Vaadin source file aside, and rename it to e.g. VScrollTable.java.orig
- Make client-side changes straight there, and test them with GWT Dev Mode or Super Dev Mode.
    - Please avoid applying code formatting while making your changes, as we want to keep the patch as small as possible.
- Once you're done with changes, run the diff command between the original Vaadin source file and your patched file:
    - e.g. diff -wur src/main/java/.../VScrollTable.java.orig src/main/java/.../VScrollTablePatched.java
- Save the output to the appropriate .txt file in src/main/patches
- If it's a newly patched class, add it to the maven-patch-plugin configuration in the pom file.
- Then before finally running the maven patch, create a copy of your patched file to a safe directory, since the one you worked on will be erased by maven before attempting to apply the patch.