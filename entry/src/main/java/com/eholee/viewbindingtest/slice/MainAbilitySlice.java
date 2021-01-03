package com.eholee.viewbindingtest.slice;

import com.eholee.viewbinding.entry.AbilityMainBinding;
import com.eholee.viewbindingtest.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.window.dialog.ToastDialog;
import ohos.hiviewdfx.HiLogLabel;

public class MainAbilitySlice extends AbilitySlice {
    private AbilityMainBinding binding;
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        binding = AbilityMainBinding.parse(this);
        super.setUIContent(binding.getRoot());
        binding.textHelloworld.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                new ToastDialog(MainAbilitySlice.this).setText("click222").show();
            }
        });


        HiLogLabel logLabel = new HiLogLabel(1,2,"22");
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
