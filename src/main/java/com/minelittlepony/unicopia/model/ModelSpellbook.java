package com.minelittlepony.unicopia.model;

import net.minecraft.client.model.ModelBook;
import net.minecraft.client.model.ModelRenderer;

public class ModelSpellbook extends ModelBook {
    public ModelSpellbook() {
        super();
        bookSpine = (new ModelRenderer(this)).setTextureOffset(12, 0);
        bookSpine.addBox(-1, -5, 0, 2, 10, 0, 0.1f);
        bookSpine.rotateAngleY = ((float)Math.PI / 2F);
    }
}
