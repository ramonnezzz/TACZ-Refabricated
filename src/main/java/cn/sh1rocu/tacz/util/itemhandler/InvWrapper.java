package cn.sh1rocu.tacz.util.itemhandler;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class InvWrapper implements IItemHandlerModifiable {
    private final Container inv;

    public InvWrapper(Container inv) {
        this.inv = inv;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        InvWrapper that = (InvWrapper) o;

        return getInv().equals(that.getInv());
    }

    @Override
    public int hashCode() {
        return getInv().hashCode();
    }

    @Override
    public int getSlots() {
        return getInv().getContainerSize();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return getInv().getItem(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        ItemStack stackInSlot = getInv().getItem(slot);

        int m;
        if (!stackInSlot.isEmpty()) {
            if (stackInSlot.getCount() >= Math.min(stackInSlot.getMaxStackSize(), getSlotLimit(slot)))
                return stack;

            if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot))
                return stack;

            if (!getInv().canPlaceItem(slot, stack))
                return stack;

            m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot)) - stackInSlot.getCount();

            if (stack.getCount() <= m) {
                if (!simulate) {
                    ItemStack copy = stack.copy();
                    copy.grow(stackInSlot.getCount());
                    getInv().setItem(slot, copy);
                    getInv().setChanged();
                }

                return ItemStack.EMPTY;
            } else {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    ItemStack copy = stack.split(m);
                    copy.grow(stackInSlot.getCount());
                    getInv().setItem(slot, copy);
                    getInv().setChanged();
                    return stack;
                } else {
                    stack.shrink(m);
                    return stack;
                }
            }
        } else {
            if (!getInv().canPlaceItem(slot, stack))
                return stack;

            m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
            if (m < stack.getCount()) {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    getInv().setItem(slot, stack.split(m));
                    getInv().setChanged();
                    return stack;
                } else {
                    stack.shrink(m);
                    return stack;
                }
            } else {
                if (!simulate) {
                    getInv().setItem(slot, stack);
                    getInv().setChanged();
                }
                return ItemStack.EMPTY;
            }
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0)
            return ItemStack.EMPTY;

        ItemStack stackInSlot = getInv().getItem(slot);

        if (stackInSlot.isEmpty())
            return ItemStack.EMPTY;

        if (simulate) {
            if (stackInSlot.getCount() < amount) {
                return stackInSlot.copy();
            } else {
                ItemStack copy = stackInSlot.copy();
                copy.setCount(amount);
                return copy;
            }
        } else {
            int m = Math.min(stackInSlot.getCount(), amount);

            ItemStack decrStackSize = getInv().removeItem(slot, m);
            getInv().setChanged();
            return decrStackSize;
        }
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        getInv().setItem(slot, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return getInv().getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return getInv().canPlaceItem(slot, stack);
    }

    public Container getInv() {
        return inv;
    }

    // ItemStack.save(CompoundTag)/ItemStack.of(CompoundTag) saíram da API; a serialização de
    // ItemStack agora é feita via codec (precisa de um RegistryOps). Não há caller pra este
    // método hoje, então RegistryAccess.EMPTY é suficiente aqui.
    public CompoundTag serializeNBT() {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, RegistryAccess.EMPTY);
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < getSlots(); i++) {
            ItemStack stack = getStackInSlot(i);
            if (!stack.isEmpty()) {
                int slot = i;
                ItemStack.CODEC.encodeStart(ops, stack).resultOrPartial().ifPresent(encoded -> {
                    CompoundTag itemTag = new CompoundTag();
                    itemTag.merge((CompoundTag) encoded);
                    itemTag.putInt("Slot", slot);
                    nbtTagList.add(itemTag);
                });
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", getSlots());
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, RegistryAccess.EMPTY);
        int size = nbt.contains("Size") ? nbt.getIntOr("Size", getSlots()) : getSlots();
        ListTag tagList = nbt.getListOrEmpty("Items");
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompoundOrEmpty(i);
            int slot = itemTags.getIntOr("Slot", -1);

            if (slot >= 0 && slot < size) {
                ItemStack.CODEC.parse(ops, itemTags).resultOrPartial().ifPresent(stack -> setStackInSlot(slot, stack));
            }
        }
    }
}
