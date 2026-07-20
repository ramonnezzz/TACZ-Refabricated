package com.tacz.guns.compat.jei;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.builder.BlockItemBuilder;
import com.tacz.guns.api.item.gun.GunItemManager;
import com.tacz.guns.compat.jei.category.AttachmentQueryCategory;
import com.tacz.guns.compat.jei.category.GunSmithTableCategory;
import com.tacz.guns.compat.jei.entry.AttachmentQueryEntry;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.init.ModRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JeiPlugin
public class GunModPlugin implements IModPlugin {
    private static final Identifier UID = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "jei");

    private Map<Identifier, RecipeType<GunSmithTableRecipe>> recipeTypeMap = new HashMap<>();

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        recipeTypeMap.clear();
        var map = TimelessAPI.getAllCommonBlockIndex();
        for (var entry : map) {
            BlockItem item = entry.getValue().getBlock();
            ItemStack icon = BlockItemBuilder.create(item).setId(entry.getKey()).build();
            RecipeType<GunSmithTableRecipe> type = RecipeType.create(GunMod.MOD_ID, "gun_smith_table/" + entry.getKey().toString().replace(':', '_'), GunSmithTableRecipe.class);
            registration.addRecipeCategories(new GunSmithTableCategory(registration.getJeiHelpers().getGuiHelper(), icon, type, item.getName(icon)));
            recipeTypeMap.put(entry.getKey(), type);
        }
        registration.addRecipeCategories(new AttachmentQueryCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // RecipeAccess do cliente não expõe mais Recipe/RecipeHolder completos por id (ver
        // ServerMessageSyncGunSmithTableRecipes) - usa o mesmo cache client-side que a
        // GunSmithTableScreen usa
        List<GunSmithTableRecipe> recipes = ModRecipe.getAllGunSmithTableRecipesClient();

        for (var entry : recipeTypeMap.entrySet()) {
            TimelessAPI.getCommonBlockIndex(entry.getKey()).ifPresent(blockIndex -> {
                List<GunSmithTableRecipe> recipeList = blockIndex.getFilter().filter(recipes, GunSmithTableRecipe::getId);
                recipeList.removeIf(recipe -> {
                    return blockIndex.getData().getTabs().stream().noneMatch(tab -> Objects.equals(tab.id(), recipe.getResult().getGroup()));
                });
                registration.addRecipes(entry.getValue(), recipeList);
            });
        }

        registration.addRecipes(AttachmentQueryCategory.ATTACHMENT_QUERY, AttachmentQueryEntry.getAllAttachmentQueryEntries());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (var entry : recipeTypeMap.entrySet()) {
            TimelessAPI.getCommonBlockIndex(entry.getKey()).ifPresent(blockIndex -> {
                ItemStack stack = BlockItemBuilder.create(blockIndex.getBlock()).setId(entry.getKey()).build();
                registration.addRecipeCatalyst(stack, entry.getValue());
            });

        }

    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        // IIngredientSubtypeInterpreter virou ISubtypeInterpreter; o overload (Item,
        // ISubtypeInterpreter<ItemStack>) já assume VanillaTypes.ITEM_STACK
        registration.registerSubtypeInterpreter(ModItems.AMMO, GunModSubtype.getAmmoSubtype());
        registration.registerSubtypeInterpreter(ModItems.ATTACHMENT, GunModSubtype.getAttachmentSubtype());
        registration.registerSubtypeInterpreter(ModItems.AMMO_BOX, GunModSubtype.getAmmoBoxSubtype());
        registration.registerSubtypeInterpreter(ModItems.WORKBENCH_111, GunModSubtype.getTableSubType());
        registration.registerSubtypeInterpreter(ModItems.WORKBENCH_121, GunModSubtype.getTableSubType());
        registration.registerSubtypeInterpreter(ModItems.WORKBENCH_211, GunModSubtype.getTableSubType());
        GunItemManager.getAllGunItems().forEach(item -> registration.registerSubtypeInterpreter(item, GunModSubtype.getGunSubtype()));
    }

    @Override
    public Identifier getPluginUid() {
        return UID;
    }
}
