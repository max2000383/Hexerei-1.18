package net.joefoxe.hexerei.events;

import net.joefoxe.hexerei.Hexerei;
import net.joefoxe.hexerei.block.ModBlocks;
import net.joefoxe.hexerei.block.custom.Candle;
import net.joefoxe.hexerei.block.custom.SageBurningPlate;
import net.joefoxe.hexerei.config.HexConfig;
import net.joefoxe.hexerei.tileentity.ModTileEntities;
import net.joefoxe.hexerei.tileentity.SageBurningPlateTile;
import net.joefoxe.hexerei.util.HexereiTags;
import net.joefoxe.hexerei.util.HexereiUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.List;
import com.google.common.base.Predicates;

@EventBusSubscriber
public class SageBurningPlateEvent {


    @SubscribeEvent
    public void onEntityJoin(LivingSpawnEvent.CheckSpawn e) {
        Level world = e.getWorld().isClientSide() ? null : e.getWorld() instanceof Level ? (Level)e.getWorld() : null;

        if (world == null) {
            return;
        }

        if(e.getSpawnReason() == MobSpawnType.CHUNK_GENERATION)
            return;

        Entity entity = e.getEntity();

        if (entity.getTags().contains(Hexerei.MOD_ID + ".checked" )) {

            return;
        }
        entity.addTag(Hexerei.MOD_ID + ".checked");

        if (!HexereiUtil.entityIsHostile(entity)) {
            return;
        }

        if(HexConfig.SAGE_BURNING_PLATE_RANGE.get()==0)return;
        //List<BlockPos> nearbySageBurningPlates = HexereiUtil.getAllTileEntityPositionsNearby(ModTileEntities.SAGE_BURNING_PLATE_TILE.get(), HexConfig.SAGE_BURNING_PLATE_RANGE.get() + 1, world, entity);

        List<Entity> nearbySageBurningPlateEntities =world.getEntities(entity,new AABB(entity.blockPosition()).inflate(HexConfig.SAGE_BURNING_PLATE_RANGE.get()),Predicates.instanceOf(ModTileEntities.SAGE_BURNING_PLATE_TILE.get().getClass()));
        //e.getClass(),new TargetingCondition(),new AABB(entity.blockPosition()).inflate(HexConfig.SAGE_BURNING_PLATE_RANGE.get())
        //world.getEntities(entity, new AABB(entity.blockPosition()).inflate(HexConfig.SAGE_BURNING_PLATE_RANGE.get()));
        //List<BlockEntity> nearbySageBurningPlateEntities = world.getEntitiesWithinAABB(Entity.class, new AABB(entity.blockPosition()).inflate(HexConfig.SAGE_BURNING_PLATE_RANGE.get()));
        //, Predicates.instanceOf(Entity.class)
        if (nearbySageBurningPlateEntities.size() == 0) {
            return;
        }

        BlockPos burning_plate = null;
        for (Entity nearbySageBurningPlateEntity : nearbySageBurningPlateEntities) {
            burning_plate = nearbySageBurningPlateEntity.blockPosition();
            BlockState burning_platestate = world.getBlockState(burning_plate);
            Block block = burning_platestate.getBlock();
            if(!burning_platestate.getValue(SageBurningPlate.LIT)) {
                continue;
            }
            if (!(block instanceof SageBurningPlate)) {
                continue;
            }

            burning_plate = burning_plate.immutable();
            break;
        }

        if (burning_plate == null) {
            return;
        }

        List<Entity> passengers = entity.getPassengers();
        if (passengers.size() > 0) {
            for (Entity passenger : passengers) {
                passenger.remove(RemovalReason.DISCARDED);
            }
        }

        e.setResult(Result.DENY);
    }
}