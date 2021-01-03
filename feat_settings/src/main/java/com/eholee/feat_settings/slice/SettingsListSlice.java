package com.eholee.feat_settings.slice;

import com.eholee.feat_settings.ResourceTable;
import com.eholee.feat_settings.persist.SettingsAbilityDatabaseHelper;
import com.eholee.feat_settings.settingscategory.SettingsItemFactory;
import com.eholee.feat_settings.views.adapter.SettingsListItemProvider;
import com.eholee.viewbinding.feat_settings.MainAbilityBinding;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.DependentLayout;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.ListContainer;

/**
 * Settings List Ability Slice
 */
public class SettingsListSlice extends AbilitySlice {
    private static SettingsAbilityDatabaseHelper settingsAbilityDatabaseHelper;
    private MainAbilityBinding binding;
    /**
     * Get the settings ability database helper
     *
     * @return SettingsAbilityDatabaseHelper
     */
    public static SettingsAbilityDatabaseHelper getSettingsAbilityDatabaseHelper() {
        return settingsAbilityDatabaseHelper;
    }

    private ComponentContainer createComponent() {
        binding = MainAbilityBinding.parse(this);
        binding.titleAreaBackIconHotArea.setClickedListener(component -> this.terminate());

        // Use a sample settings data initialized from json file.
        // You can provide you own data by implement SettingsItemFactory to achieve different effects
        SettingsListItemProvider itemProvider = new SettingsListItemProvider(SettingsItemFactory.initSettings(this));

        // ListContainer item provider and listener
        if (binding.listView != null) {
            binding.listView.setItemProvider(itemProvider);
            binding.listView.setItemClickedListener(itemProvider);
        }

        return binding.getRoot();
    }

    @Override
    public void onStart(Intent intent) {
        // Database helper
        settingsAbilityDatabaseHelper = new SettingsAbilityDatabaseHelper(this);
        setUIContent(createComponent());
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
