package alpha4;

import aic2019.Direction;
import aic2019.Location;
import aic2019.UnitController;

public class Movement {

    UnitController uc;
    Data data;

    public Movement (UnitController _uc, Data _data) {
        this.uc = _uc;
        this.data = _data;
    }

    private final int INF = Integer.MAX_VALUE;

    private boolean rotateRight = true;         // if I should rotate right or left
    private Location lastObstacleFound = null;  // latest obstacle I've found in my way
    private int minDistToTarget = INF;          // minimum distance I've been to the target while going around an obstacle
    private Location prevTarget = null;         // previous target

    void moveTo(Location target){
        // No target? ==> bye!
        if (target == null) return;

        // Different target? ==> previous data does not help!
        if (prevTarget == null || !target.isEqual(prevTarget)) resetMovement();

        // If I'm at a minimum distance to the target, I'm free!
        Location myLoc = uc.getLocation();
        int d = myLoc.distanceSquared(target);
        if (d <= minDistToTarget) resetMovement();

        // update data
        prevTarget = target;
        minDistToTarget = Math.min(d, minDistToTarget);

        // If there's an obstacle I try to go around it [until I'm free] instead of going to the target directly
        Direction dir = myLoc.directionTo(target);
        if (lastObstacleFound != null) dir = myLoc.directionTo(lastObstacleFound);

        // This should not happen for a single unit, but whatever
        if (uc.canMove(dir)) resetMovement();

        // I rotate clockwise or counterclockwise (depends on 'rotateRight'). If I try to go out of the map I change the orientation
        // Note that we have to try at most 16 times since we can switch orientation in the middle of the loop. (It can be done more efficiently)
        for (int i = 0; i < 16; ++i) {
            if (uc.canMove(dir)) {
                uc.move(dir);
                return;
            }
            Location newLoc = myLoc.add(dir);
            if (uc.isOutOfMap(newLoc)) rotateRight = !rotateRight;
            // If I could not go in that direction and it was not outside of the map, then this is the latest obstacle found
            else lastObstacleFound = myLoc.add(dir);

            if (rotateRight) dir = dir.rotateRight();
            else dir = dir.rotateLeft();
        }

        if (uc.canMove(dir)) uc.move(dir);
    }

    // clear some of the previous data
    void resetMovement(){
        lastObstacleFound = null;
        minDistToTarget = INF;
    }


}