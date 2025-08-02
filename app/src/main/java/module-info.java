module org.ebenlib {
    // Required by most Java CLI apps
    requires java.base;

    // Required if you're using any AWT/Swing features (even for colors or fonts)
    requires java.desktop;

    // Export only your public-facing packages
    exports org.ebenlib.cli;
    exports org.ebenlib.book;
    exports org.ebenlib.borrow;
    exports org.ebenlib.user;
    exports org.ebenlib.profile;
    exports org.ebenlib.report;
    exports org.ebenlib.system;
    exports org.ebenlib.searchsort;
    exports org.ebenlib.utils;
    exports org.ebenlib.ds;
}
