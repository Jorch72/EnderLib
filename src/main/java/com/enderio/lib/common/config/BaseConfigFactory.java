package com.enderio.lib.common.config;

import java.util.Set;

import com.enderio.lib.client.config.BaseConfigGui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.client.IModGuiFactory;

public class BaseConfigFactory implements IModGuiFactory {
  @Override
  public void initialize(Minecraft minecraftInstance) {
    ;
  }

  @Override
  public Class<? extends GuiScreen> mainConfigGuiClass() {
    return BaseConfigGui.class;
  }

  @Override
  public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
    return null;
  }

  @Override
  public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
    return null;
  }

}
