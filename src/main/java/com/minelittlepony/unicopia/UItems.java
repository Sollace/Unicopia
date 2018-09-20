package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.item.ItemApple;
import com.minelittlepony.unicopia.item.ItemCloud;
import com.minelittlepony.unicopia.item.ItemCurse;
import com.minelittlepony.unicopia.item.ItemOfHolding;
import com.minelittlepony.unicopia.item.ItemSpell;
import com.minelittlepony.unicopia.item.UItemBlock;
import com.minelittlepony.unicopia.item.UItemMultiTexture;
import com.minelittlepony.unicopia.item.UItemSlab;
import com.minelittlepony.unicopia.spell.SpellRegistry;

import come.minelittlepony.unicopia.forgebullshit.RegistryLockSpinner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.registries.IForgeRegistry;

import static com.minelittlepony.unicopia.Predicates.*;

public class UItems {
    public static final ItemApple apple = new ItemApple()
            .setSubTypes("apple", "green", "sweet", "rotten", "zap", "zap_cooked")
            .setTypeRarities(10, 20, 10, 30);

    public static final Item cloud_matter = new Item()
            .setCreativeTab(CreativeTabs.MATERIALS)
            .setTranslationKey("cloud_matter")
            .setRegistryName(Unicopia.MODID, "cloud_matter");

    public static final Item dew_drop = new Item()
            .setCreativeTab(CreativeTabs.MATERIALS)
            .setTranslationKey("dew_drop")
            .setRegistryName(Unicopia.MODID, "dew_drop");

    public static final ItemCloud cloud_spawner = new ItemCloud(Unicopia.MODID, "cloud");

    public static final Item cloud_block = new UItemMultiTexture(UBlocks.cloud, stack -> {
        return CloudType.byMetadata(stack.getMetadata()).getTranslationKey();
    }, INTERACT_WITH_CLOUDS)
            .setRegistryName(Unicopia.MODID, "cloud_block");

    public static final Item cloud_stairs = new UItemBlock(UBlocks.stairsCloud, INTERACT_WITH_CLOUDS)
            .setTranslationKey("cloud_stairs")
            .setRegistryName(Unicopia.MODID, "cloud_stairs");


    public static final Item anvil = new UItemBlock(UBlocks.anvil, INTERACT_WITH_CLOUDS)
            .setTranslationKey("cloud_anvil")
            .setRegistryName(Unicopia.MODID, "anvil");

    public static final Item mist_door = new ItemDoor(UBlocks.mist_door)
            .setTranslationKey("mist_door")
            .setRegistryName(Unicopia.MODID, "mist_door");

    public static final Item cloud_slab = new UItemSlab(UBlocks.cloud_slab, UBlocks.cloud_slab, UBlocks.cloud_double_slab, INTERACT_WITH_CLOUDS)
            .setTranslationKey("cloud_slab")
            .setRegistryName(Unicopia.MODID, "cloud_slab");

    public static final ItemSpell spell = new ItemSpell(Unicopia.MODID, "gem");
    public static final ItemSpell curse = new ItemCurse(Unicopia.MODID, "corrupted_gem");

    public static final ItemOfHolding bag_of_holding = new ItemOfHolding(Unicopia.MODID, "bag_of_holding");

    static void registerItems(IForgeRegistry<Item> registry) {
        RegistryLockSpinner.unlock(Item.REGISTRY);

        Item.REGISTRY.register(Item.getIdFromItem(Items.APPLE), new ResourceLocation("apple"), apple);

        RegistryLockSpinner.lock(Item.REGISTRY);

        registry.registerAll(cloud_spawner, dew_drop, cloud_matter, cloud_block,
                             cloud_stairs, cloud_slab, mist_door, anvil,
                             bag_of_holding, spell, curse);

        if (UClient.isClientSide()) {
            registerAllVariants(apple, apple.getVariants());
            registerAllVariants(cloud_spawner, "cloud_small", "cloud_medium", "cloud_large");
            registerAllVariants(dew_drop, "dew_drop");
            registerAllVariants(cloud_matter, "cloud_matter");
            registerAllVariants(cloud_stairs, "cloud_stairs");
            registerAllVariants(cloud_slab, CloudType.getVariants("_cloud_slab"));
            registerAllVariants(cloud_block, CloudType.getVariants("_cloud_block"));
            registerAllVariants(mist_door, "mist_door");
            registerAllVariants(anvil, "anvil");
            registerAllVariants(bag_of_holding, "bag_of_holding");
            registerAllVariants(spell, "gem");
            registerAllVariants(curse, "corrupted_gem");
        }

        registerFuels();
    }

    private static void registerAllVariants(Item item, String... variants) {
        for (int i = 0; i < variants.length; i++) {
            ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation("unicopia:" + variants[i]));
        }
    }

    static void registerFuels() {
        int zap = apple.getZapAppleMetadata();
        FurnaceRecipes.instance().addSmeltingRecipe(
                new ItemStack(UItems.apple, 1, zap),
                new ItemStack(UItems.apple, 1, zap + 1), 0.1F);
    }

    static void registerRecipes(IForgeRegistry<IRecipe> registry) {
        Ingredient dewdrop = Ingredient.fromItem(dew_drop);
        Ingredient cloud = Ingredient.fromStacks(new ItemStack(cloud_block, 1, 0));

        ItemStack bookStack = new ItemStack(Items.ENCHANTED_BOOK, 1);
        bookStack.addEnchantment(Enchantments.FEATHER_FALLING, 1);

        Ingredient book = Ingredient.fromStacks(bookStack);

        registry.register(new ShapedRecipes("", 3, 3, NonNullList.from(Ingredient.EMPTY,
                dewdrop, dewdrop, dewdrop,
                cloud, book, cloud,
                cloud, cloud, cloud
        ), new ItemStack(cloud_block, 1, 2)).setRegistryName(Unicopia.MODID, "id_dont_care_just_use_it"));
    }

    static void registerColors(ItemColors registry) {
        registry.registerItemColorHandler((stack, tint) -> {
            if (Predicates.MAGI.test(Minecraft.getMinecraft().player)) {
                return SpellRegistry.instance().getSpellTintFromStack(stack);
            }

            return 0xffffff;
        }, spell, curse);
    }
}
