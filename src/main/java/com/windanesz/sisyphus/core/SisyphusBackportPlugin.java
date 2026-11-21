package com.windanesz.sisyphus.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.Name("SisyphusBackportCore")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions("com.windanesz.sisyphus.core")
public class SisyphusBackportPlugin implements IFMLLoadingPlugin {

    /**
     * FermiumBooter wants its mixins queued while its plugins are constructed.
     */
    public SisyphusBackportPlugin() {
        MixinSupport.queueEarly();
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        MixinSupport.queueWhenDataIsInjected();
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
