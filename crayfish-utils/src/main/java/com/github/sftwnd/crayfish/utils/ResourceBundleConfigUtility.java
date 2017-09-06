package com.github.sftwnd.crayfish.utils;

import java.util.ResourceBundle;

/**
 * Created by ashindarev on 04.02.16.
 */
public class ResourceBundleConfigUtility extends ConfigUtility {


    private ResourceBundle resourceBundle;

    public ResourceBundleConfigUtility(ResourceBundle resourceBundle, String dateFormat) {
        super(dateFormat);
        setResourceBundle(resourceBundle);
    }

    public ResourceBundleConfigUtility(ResourceBundle resourceBundle) {
        this(resourceBundle, null);
    }

    public ResourceBundleConfigUtility(String resourceBundle) {
        this(resourceBundle, null);
    }

    public ResourceBundleConfigUtility(String resourceBundle, String dateFormat) {
        this(Utf8ResourceBundle.getBundle(resourceBundle), dateFormat);
    }

    @Override
    public String loadValue(String key) {
        return this.getResourceBundle() == null
                ? null
                : this.getResourceBundle().containsKey(key)
                  ? this.getResourceBundle().getString(key)
                  : null;
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public final void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

}
