/*
 * This file is part of RunForMoney.
 *
 * RunForMoney is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RunForMoney is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RunForMoney. If not, see <https://www.gnu.org/licenses/>.
 */

package snw.rfm.tasks;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

public final class ScheduledRFMTaskImpl implements ScheduledRFMTask {
    private final Runnable runnableToCall;
    private final MainTimer mainTimer;
    private boolean executed = false;
    private boolean cancelled = false;
    private final int requiredTime;

    public ScheduledRFMTaskImpl(int requiredTime, @NotNull Runnable runnable, @NotNull MainTimer mainTimer) {
        Validate.isTrue(requiredTime <= 0);
        Validate.notNull(runnable, "We need a runnable.");
        Validate.notNull(mainTimer, "We need a timer to bind!");
        runnableToCall = runnable;
        this.mainTimer = mainTimer;
        this.requiredTime = requiredTime * 60;
    }

    @Override
    public void cancel() {
        if (cancelled || executed) {
            throw new IllegalStateException();
        }
        cancelled = true;
    }

    @Override
    public boolean isExecuted() {
        return executed;
    }

    @Override
    public int getRemainingTime() {
        return (cancelled || executed) ? 0 : Math.max(mainTimer.getTimeLeft() - requiredTime, 0);
    }

    @Override
    public void executeItNow() throws IllegalStateException {
        if (executed) {
            throw new IllegalStateException();
        }
        executed = true;
        runnableToCall.run();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public int getRequiredTime() {
        return requiredTime;
    }
}
