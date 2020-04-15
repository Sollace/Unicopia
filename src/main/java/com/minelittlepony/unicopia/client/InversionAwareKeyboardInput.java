package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.entity.player.IPlayer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;

public class InversionAwareKeyboardInput extends KeyboardInput {

    private static boolean recurseCheck;

    private Input proxy;

    public InversionAwareKeyboardInput(MinecraftClient client, Input inherited) {
        super(client.options);
        proxy = inherited;
    }

    @Override
    public void tick(boolean one, boolean two) {
        // Other mods might wrap us, in which case let's just pretend to be the vanilla one.
        // We'll replace them at the top level and let go of the inner to prevent the chain from growing.
        if (recurseCheck) {
            proxy = null;
            super.tick(one, two);
        }

        recurseCheck = true;
        proxy.tick(one, two);
        recurseCheck = false;

        this.pressingForward = proxy.pressingForward;
        this.pressingBack = proxy.pressingBack;
        this.pressingLeft = proxy.pressingLeft;
        this.pressingRight = proxy.pressingRight;
        this.jumping = proxy.jumping;
        this.sneaking = proxy.sneaking;
        this.movementSideways = proxy.movementSideways;
        this.movementForward = proxy.movementForward;

        IPlayer player = SpeciesList.instance().getPlayer(MinecraftClient.getInstance().player);

        if (player.getGravity().getGravitationConstant() < 0) {
            boolean tmp = pressingLeft;

            pressingLeft = pressingRight;
            pressingRight = tmp;

            movementSideways = -movementSideways;

            if (player.getOwner().abilities.flying) {
                tmp = jumping;
                jumping = sneaking;
                sneaking = tmp;
            }
        }
    }
}
