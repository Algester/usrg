package com.rafacost3d.ultimatecobblegen;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CobblestoneGenerator extends Block implements ITileEntityProvider {

    static final ResourceLocation RS = new ResourceLocation(UltimateCobbleGen.MODID, "cobblegenerator");
    static final CobblestoneGenerator BLOCK = new CobblestoneGenerator();
    static final Item ITEM = new Item();

    public CobblestoneGenerator(){
        super(Material.ROCK);
        this.setResistance(15f);
        this.setHardness(5f);
        this.setHarvestLevel("pickaxe", 0);
        this.setRegistryName(RS);
        this.setUnlocalizedName(RS.toString());
        //Git
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int meta){
        return new Tile();
    }

    public static class Item extends ItemBlock {
        public Item() {
            super(BLOCK);
            setRegistryName(RS);
        }
    }

    public static class Tile extends TileEntity implements IItemHandler {

        EnergyStorage storage;

        public Tile() {
            if (Configs.RF_PER_BLOCK > 0) {
                storage = new EnergyStorage(Configs.RF_PER_BLOCK);
            }
        }

        @Override
        public void readFromNBT(NBTTagCompound compound) {
            super .readFromNBT(compound);
            if (compound.hasKey("EnergyStorage") && storage != null) {
                CapabilityEnergy.ENERGY.readNBT(storage, null, compound.getTag("EnergyStorage"));
            }
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            if (storage != null) {
                compound.setTag("EnergyStorage", CapabilityEnergy.ENERGY.writeNBT(storage, null));
            }
            return super.writeToNBT(compound);
        }

        @Override
        public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
            if (CapabilityEnergy.ENERGY.equals(capability) && storage != null) {
                return true;
            } else if (CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.equals(capability)) {
                return true;
            } else {
                return super.hasCapability(capability, facing);
            }
        }

        @Nullable
        @Override
        public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
            if (CapabilityEnergy.ENERGY.equals(capability) && storage != null) {
                return CapabilityEnergy.ENERGY.cast(storage);
            } else if (CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.equals(capability)) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this);
            } else {
                return super.getCapability(capability, facing);
            }
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return new ItemStack(Blocks.COBBLESTONE, getMaxCobble());
        }

        public int getMaxCobble() {
            int cobble = storage != null ? storage.getEnergyStored() / Configs.RF_PER_BLOCK : Integer.MAX_VALUE;
            return Configs.MAX_COBBLE_PER_TICK == -1 ? cobble : Math.min(cobble, Configs.MAX_COBBLE_PER_TICK);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            int amt = Math.min(getMaxCobble(), amount);
            if (storage != null) {
                storage.extractEnergy(amt * Configs.RF_PER_BLOCK, simulate);
            }
            return new ItemStack(Blocks.COBBLESTONE, amt);
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }
    }
}
