package df11zomgraves.ingameinfo.util;

import com.google.common.collect.AbstractIterator;

import net.minecraft.core.BlockPos;

import java.util.Iterator;

public class BlockPosHelper {
    // Axis indices into the coordinate arrays below.
    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;

    public static Iterable<MBlockPos> getAllInBox(final BlockPos from, final BlockPos to) {
        return getAllInBox(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
    }

    public static Iterable<MBlockPos> getAllInBox(final int fromX, final int fromY, final int fromZ, final int toX, final int toY, final int toZ) {
        return getAllInBox(fromX, fromY, fromZ, toX, toY, toZ, new int[] { X, Y, Z });
    }

    public static Iterable<MBlockPos> getAllInBoxXZY(final BlockPos from, final BlockPos to) {
        return getAllInBoxXZY(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
    }

    public static Iterable<MBlockPos> getAllInBoxXZY(final int fromX, final int fromY, final int fromZ, final int toX, final int toY, final int toZ) {
        return getAllInBox(fromX, fromY, fromZ, toX, toY, toZ, new int[] { X, Z, Y });
    }

    public static Iterable<MBlockPos> getAllInBoxYXZ(final BlockPos from, final BlockPos to) {
        return getAllInBoxYXZ(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
    }

    public static Iterable<MBlockPos> getAllInBoxYXZ(final int fromX, final int fromY, final int fromZ, final int toX, final int toY, final int toZ) {
        return getAllInBox(fromX, fromY, fromZ, toX, toY, toZ, new int[] { Y, X, Z });
    }

    // order lists the axes from fastest to slowest advancing; when an axis
    // advances, every faster axis restarts at its minimum.
    private static Iterable<MBlockPos> getAllInBox(final int fromX, final int fromY, final int fromZ, final int toX, final int toY, final int toZ, final int[] order) {
        final int[] min = { Math.min(fromX, toX), Math.min(fromY, toY), Math.min(fromZ, toZ) };
        final int[] max = { Math.max(fromX, toX), Math.max(fromY, toY), Math.max(fromZ, toZ) };
        final BlockPos posMax = new BlockPos(max[X], max[Y], max[Z]);
        return new Iterable<MBlockPos>() {
            @Override
            public Iterator<MBlockPos> iterator() {
                return new AbstractIterator<MBlockPos>() {
                    private MBlockPos pos = null;
                    private final int[] cur = new int[3];

                    @Override
                    protected MBlockPos computeNext() {
                        if (this.pos == null) {
                            System.arraycopy(min, 0, this.cur, 0, 3);
                            this.pos = new MBlockPos(this.cur[X], this.cur[Y], this.cur[Z]);
                            return this.pos;
                        }

                        if (this.pos.equals(posMax)) {
                            return this.endOfData();
                        }

                        for (int i = 0; i < order.length; i++) {
                            final int axis = order[i];
                            if (this.cur[axis] < max[axis]) {
                                this.cur[axis]++;
                                for (int j = 0; j < i; j++) {
                                    this.cur[order[j]] = min[order[j]];
                                }
                                break;
                            }
                        }

                        this.pos.x = this.cur[X];
                        this.pos.y = this.cur[Y];
                        this.pos.z = this.cur[Z];
                        return this.pos;
                    }
                };
            }
        };
    }
}
