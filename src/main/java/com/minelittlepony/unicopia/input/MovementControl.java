package com.minelittlepony.unicopia.input;

import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.forgebullshit.FUF;
import com.minelittlepony.unicopia.player.IPlayer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;

@FUF(reason = "Back to the ancient art of proxy classes...")
public class MovementControl extends MovementInputFromOptions {

    static boolean recurseCheck;

    private MovementInput wrappedInstance;

    public MovementControl(MovementInput inherited) {
        super(Minecraft.getMinecraft().gameSettings);
        wrappedInstance = inherited;
    }

    @Override
    public void updatePlayerMoveState() {
        // Other mods might wrap us, in which case let's just pretend to be the vanilla one.
        // We'll replace them at the top level and let go of the inner to prevent the chain from growing.
        if (recurseCheck) {
            wrappedInstance = null;
            super.updatePlayerMoveState();
        }

        recurseCheck = true;
        wrappedInstance.updatePlayerMoveState();
        recurseCheck = false;

        this.moveStrafe = wrappedInstance.moveStrafe;
        this.moveForward = wrappedInstance.moveForward;
        this.forwardKeyDown = wrappedInstance.forwardKeyDown;
        this.backKeyDown = wrappedInstance.backKeyDown;
        this.leftKeyDown = wrappedInstance.leftKeyDown;
        this.rightKeyDown = wrappedInstance.rightKeyDown;
        this.jump = wrappedInstance.jump;
        this.sneak = wrappedInstance.sneak;

        IPlayer player = UClient.instance().getIPlayer();

        if (player.getGravity().getGravitationConstant() < 0) {
            boolean tmp = leftKeyDown;

            leftKeyDown = rightKeyDown;
            rightKeyDown = tmp;

            moveStrafe = -moveStrafe;

            if (player.getOwner().capabilities.isCreativeMode && player.getOwner().capabilities.isFlying) {
                tmp = jump;
                jump = sneak;
                sneak = tmp;
            }
        }
    }
}
