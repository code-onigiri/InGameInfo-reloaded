package df11zomgraves.ingameinfo.tag;

import df11zomgraves.ingameinfo.handler.ConfigurationHandler;
import df11zomgraves.ingameinfo.util.StringConvertUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.core.registries.BuiltInRegistries;

public abstract class TagMouseOver extends Tag {
	@Override
	public String getCategory() {
		return "mouseover";
	}

	public static class Name extends TagMouseOver {
		@Override
		public String getValue() {
			final HitResult hitResult = minecraft.hitResult;
			if (hitResult != null) {
				final Type hitType = hitResult.getType();
				if (hitType == HitResult.Type.ENTITY) {
					final EntityHitResult targetEntity = (EntityHitResult) hitResult;
					return targetEntity.getEntity().getDisplayName().getString();
				} else if (hitType == HitResult.Type.BLOCK) {
					final BlockHitResult targetBlock = (BlockHitResult) hitResult;
					final BlockPos pos = targetBlock.getBlockPos();
					final BlockState blockState = world.getBlockState(pos);
					final Block block = blockState.getBlock();
					return blockState.getCloneItemStack(hitResult, world, pos, player).getHoverName().getString();
				}
			}
			return "";
		}
	}

	public static class UniqueName extends TagMouseOver {
		@Override
		public String getValue() {
			final HitResult hitResult = minecraft.hitResult;
			if (hitResult != null) {
				final Type hitType = hitResult.getType();
				ResourceLocation res;
				if (hitType == HitResult.Type.ENTITY) {
					final EntityHitResult targetEntity = (EntityHitResult) hitResult;
					res = BuiltInRegistries.ENTITY_TYPE.getKey(targetEntity.getEntity().getType());
					return res.toString();
				} else if (hitType == HitResult.Type.BLOCK) {
					final BlockHitResult targetBlock = (BlockHitResult) hitResult;
					final BlockPos pos = targetBlock.getBlockPos();
					final BlockState blockState = world.getBlockState(pos);
					res = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
					return res.toString();
				}
			}
			return "";
		}
	}

	public static class Id extends TagMouseOver {
		@Override
		public String getValue() {
			final HitResult hitResult = minecraft.hitResult;
			if (hitResult != null) {
				final Type hitType = hitResult.getType();
				if (hitType == HitResult.Type.ENTITY) {
					final EntityHitResult targetEntity = (EntityHitResult) hitResult;
					return String.valueOf(targetEntity.getEntity().getId());
				} else if (hitType == HitResult.Type.BLOCK) {
					final BlockHitResult targetBlock = (BlockHitResult) hitResult;
					final Block block = world.getBlockState(targetBlock.getBlockPos()).getBlock();
					return String.valueOf(BuiltInRegistries.BLOCK.getId(block));
				}
			}
			return "0";
		}
	}

	/**
	 * Block metadata no longer exists in 1.20.1. Returning "0" keeps the tag
	 * present and deterministic instead of removing it from configs.
	 */
	public static class Metadata extends TagMouseOver {
		@Override
		public String getValue() {
			final HitResult hitResult = minecraft.hitResult;
			if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
				return "0";
			}
			return "0";
		}
	}

	public static class IsBlock extends TagMouseOver {
		@Override
		public String getValue() {
			final HitResult hitResult = minecraft.hitResult;
			final Type hitType = hitResult.getType();
			return String.valueOf(hitType == HitResult.Type.BLOCK);
		}
	}

	public static class PowerWeak extends TagMouseOver {
		@Override
		public String getValue() {
			final HitResult hitResult = minecraft.hitResult;
			if (hitResult != null) {
				final Type hitType = hitResult.getType();
				if (hitType == HitResult.Type.BLOCK) {
					int power = -1;
					final BlockHitResult targetBlock = (BlockHitResult) hitResult;
					for (final Direction side : Direction.values()) {
						final BlockPos pos = targetBlock.getBlockPos().relative(side);
						final BlockState blockState = world.getBlockState(pos);
						power = Math.max(power, blockState.getSignal(world, pos, side));
						if (power >= 15)
							break;
					}
					return String.valueOf(power);
				}
			}
			return "-1";
		}
	}

	public static class PowerStrong extends TagMouseOver {
		@Override
		public String getValue() {
			final HitResult hitResult = minecraft.hitResult;
			if (hitResult != null) {
				final Type hitType = hitResult.getType();
				if (hitType == HitResult.Type.BLOCK) {
					int power = -1;
					final BlockHitResult targetBlock = (BlockHitResult) hitResult;
					final BlockPos pos = targetBlock.getBlockPos();
					final BlockState blockState = world.getBlockState(pos);
					for (final Direction side : Direction.values()) {
						power = Math.max(power, blockState.getDirectSignal(world, pos, side));
						if (power >= 15)
							break;
					}
					return String.valueOf(power);
				}
			}
			return "-1";
		}
	}

	public static class PowerInput extends TagMouseOver {
		@Override
		public String getValue() {
			final HitResult hitResult = minecraft.hitResult;
			if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
				final BlockHitResult targetBlock = (BlockHitResult) hitResult;
				return String.valueOf(world.hasNeighborSignal(targetBlock.getBlockPos()));
			}
			return "-1";
		}
	}
	
	public static class LookingAtXYZ extends TagMouseOver {
		@Override
		public String getValue() {
			final HitResult hitResult = minecraft.hitResult;
			if (hitResult != null) {
				final Type hitType = hitResult.getType();
				if (hitType == HitResult.Type.BLOCK) {
					final BlockHitResult targetBlock = (BlockHitResult) hitResult;
					final BlockPos pos = targetBlock.getBlockPos();
					return String.format("%s, %s, %s", pos.getX(), pos.getY(), pos.getZ());
				}
			}
			return "";
		}
	}

	public static class TargetHealth extends TagMouseOver {
		@Override
		public String getValue() {
			final HitResult hitResult = minecraft.hitResult;
			if (hitResult != null) {
				final Type hitType = hitResult.getType();
				if (hitType == HitResult.Type.ENTITY) {
					final EntityHitResult targetEntity = (EntityHitResult) hitResult;
					Entity entity = targetEntity.getEntity();
					if (entity instanceof Mob) {
						float mobHealth = ((Mob)entity).getHealth();//getMaxHealth
						return StringConvertUtils.getFloatDisplayFormat(mobHealth, ConfigurationHandler.healthDecimalPlace);
					}
				}
			}
			return "-1";
		}
	}
	
	public static class TargetMaxHealth extends TagMouseOver {
		@Override
		public String getValue() {
			final HitResult hitResult = minecraft.hitResult;
			if (hitResult != null) {
				final Type hitType = hitResult.getType();
				if (hitType == HitResult.Type.ENTITY) {
					final EntityHitResult targetEntity = (EntityHitResult) hitResult;
					Entity entity = targetEntity.getEntity();
					if (entity instanceof Mob) {
						float mobHealth = ((Mob)entity).getMaxHealth();
						return StringConvertUtils.getFloatDisplayFormat(mobHealth, ConfigurationHandler.healthDecimalPlace);
					}
				}
			}
			return "-1";
		}
	}

	public static void register() {
		TagRegistry.INSTANCE.register(new Name().setName("mouseovername"));
		TagRegistry.INSTANCE.register(new UniqueName().setName("mouseoveruniquename"));
		TagRegistry.INSTANCE.register(new PowerWeak().setName("mouseoverpowerweak"));
		TagRegistry.INSTANCE.register(new PowerStrong().setName("mouseoverpowerstrong"));
		TagRegistry.INSTANCE.register(new IsBlock().setName("mouseoverisblock"));
		TagRegistry.INSTANCE.register(new LookingAtXYZ().setName("lookingblockxyz"));
		TagRegistry.INSTANCE.register(new Id().setName("mouseoverid"));
		TagRegistry.INSTANCE.register(new TargetHealth().setName("targethealth"));
		TagRegistry.INSTANCE.register(new TargetMaxHealth().setName("targetmaxhealth"));
		TagRegistry.INSTANCE.register(new Metadata().setName("mouseovermetadata"));
		TagRegistry.INSTANCE.register(new PowerInput().setName("mouseoverpowerinput"));
	}
}
