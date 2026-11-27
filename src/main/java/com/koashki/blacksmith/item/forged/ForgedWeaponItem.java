package com.koashki.blacksmith.item.forged;

import com.koashki.blacksmith.component.ForgedCompositionData;
import com.koashki.blacksmith.component.ItemGradeData;
import com.koashki.blacksmith.core.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public class ForgedWeaponItem extends SwordItem {
    private final ForgedArchetype archetype;

    public ForgedWeaponItem(ForgedArchetype archetype, Properties properties) {
        super(Tiers.IRON, properties.attributes(SwordItem.createAttributes(Tiers.IRON, Math.round(archetype.baseDamage() - 3.0f), archetype.attackSpeed())));
        this.archetype = archetype;
    }

    public ForgedArchetype archetype() {
        return archetype;
    }

    @Override
    public Component getName(ItemStack stack) {
        ItemGradeData grade = stack.get(ModDataComponents.ITEM_GRADE.get());
        if (grade != null) {
            if (grade.customName().isPresent()) {
                return Component.literal(grade.customName().get()).withColor(grade.tier().colorRgb());
            }
            ForgedCompositionData comp = stack.get(ModDataComponents.FORGED_COMPOSITION.get());
            String formName = comp != null ? ForgedArchetype.byId(comp.archetypeId()).displayName() : archetype.displayName();
            return grade.tier().displayName()
                    .append(Component.literal(" " + formName).withColor(grade.tier().colorRgb()));
        }
        return super.getName(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        ItemGradeData grade = stack.get(ModDataComponents.ITEM_GRADE.get());
        if (grade != null && grade.tier().isMasterpiece()) {
            return true;
        }
        return super.isFoil(stack);
    }
}
