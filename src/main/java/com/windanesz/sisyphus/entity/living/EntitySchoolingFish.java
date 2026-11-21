package com.windanesz.sisyphus.entity.living;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class EntitySchoolingFish extends EntityFishBase {

    private EntitySchoolingFish leader;
    private int schoolSize = 1;

    public EntitySchoolingFish(World world) {
        super(world);
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        this.tasks.addTask(5, new AIFollowLeader(this));
    }

    public int getMaxSchoolSize() {
        return super.getMaxSpawnedInChunk();
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return this.getMaxSchoolSize();
    }

    @Override
    protected boolean canRandomSwim() {
        return !this.isFollower();
    }

    public boolean isFollower() {
        return this.leader != null && this.leader.isEntityAlive();
    }

    public EntitySchoolingFish startFollowing(EntitySchoolingFish leader) {
        this.leader = leader;
        leader.addFollower();
        return leader;
    }

    public void stopFollowing() {
        this.leader.removeFollower();
        this.leader = null;
    }

    private void addFollower() {
        ++this.schoolSize;
    }

    private void removeFollower() {
        --this.schoolSize;
    }

    public boolean canBeFollowed() {
        return this.hasFollowers() && this.schoolSize < this.getMaxSchoolSize();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.hasFollowers() && this.world.rand.nextInt(200) == 1) {
            List<? extends EntitySchoolingFish> nearby = this.world.getEntitiesWithinAABB(this.getClass(), this.getEntityBoundingBox().grow(8.0D, 8.0D, 8.0D));
            if (nearby.size() <= 1) {
                this.schoolSize = 1;
            }
        }
    }

    public boolean hasFollowers() {
        return this.schoolSize > 1;
    }

    public boolean inRangeOfLeader() {
        return this.getDistanceSq(this.leader) <= 121.0D;
    }

    public void pathToLeader() {
        if (this.isFollower()) {
            this.getNavigator().tryMoveToEntityLiving(this.leader, 1.0D);
        }
    }

    public void addFollowers(List<? extends EntitySchoolingFish> followers) {
        int room = this.getMaxSchoolSize() - this.schoolSize;
        for (EntitySchoolingFish fish : followers) {
            if (room <= 0) {
                break;
            }
            if (fish != this) {
                fish.startFollowing(this);
                room--;
            }
        }
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData data) {
        data = super.onInitialSpawn(difficulty, data);
        if (data == null) {
            data = new SchoolSpawnData(this);
        } else {
            this.startFollowing(((SchoolSpawnData) data).leader);
        }
        return data;
    }

    public static class SchoolSpawnData implements IEntityLivingData {

        public final EntitySchoolingFish leader;

        public SchoolSpawnData(EntitySchoolingFish leader) {
            this.leader = leader;
        }
    }

    static class AIFollowLeader extends EntityAIBase {

        private final EntitySchoolingFish fish;
        private int timeToRecalcPath;
        private int nextStartTick;

        AIFollowLeader(EntitySchoolingFish fish) {
            this.fish = fish;
            this.nextStartTick = this.nextStartTick();
        }

        private int nextStartTick() {
            return 200 + this.fish.getRNG().nextInt(200) % 20;
        }

        @Override
        public boolean shouldExecute() {
            if (this.fish.hasFollowers()) {
                return false;
            } else if (this.fish.isFollower()) {
                return true;
            } else if (this.nextStartTick > 0) {
                --this.nextStartTick;
                return false;
            } else {
                this.nextStartTick = this.nextStartTick();
                List<? extends EntitySchoolingFish> nearby = this.fish.world.getEntitiesWithinAABB(this.fish.getClass(),
                        this.fish.getEntityBoundingBox().grow(8.0D, 8.0D, 8.0D),
                        other -> other != null && (other.canBeFollowed() || !other.isFollower()));
                EntitySchoolingFish leader = this.fish;
                for (EntitySchoolingFish other : nearby) {
                    if (other.canBeFollowed()) {
                        leader = other;
                        break;
                    }
                }
                List<EntitySchoolingFish> followers = new ArrayList<>();
                for (EntitySchoolingFish other : nearby) {
                    if (!other.isFollower()) {
                        followers.add(other);
                    }
                }
                leader.addFollowers(followers);
                return this.fish.isFollower();
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.fish.isFollower() && this.fish.inRangeOfLeader();
        }

        @Override
        public void startExecuting() {
            this.timeToRecalcPath = 0;
        }

        @Override
        public void resetTask() {
            if (this.fish.isFollower()) {
                this.fish.stopFollowing();
            }
        }

        @Override
        public void updateTask() {
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                this.fish.pathToLeader();
            }
        }
    }
}
