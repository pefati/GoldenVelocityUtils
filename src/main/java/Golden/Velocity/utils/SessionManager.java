package Golden.Velocity.utils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final Map<UUID, Long> sessionStartTimes;
    
    public SessionManager() {
        this.sessionStartTimes = new ConcurrentHashMap<>();
    }
    
    /**
     * Start tracking a session for a player
     */
    public void startSession(UUID playerUuid) {
        sessionStartTimes.put(playerUuid, System.currentTimeMillis());
    }
    
    /**
     * End a session and remove from tracking
     */
    public void endSession(UUID playerUuid) {
        sessionStartTimes.remove(playerUuid);
    }
    
    /**
     * Get the current session time for a player in milliseconds
     * Returns 0 if player has no active session
     */
    public long getSessionTime(UUID playerUuid) {
        Long startTime = sessionStartTimes.get(playerUuid);
        if (startTime == null) {
            return 0L;
        }
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * Format time in a readable format
     */
    public String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        seconds = seconds % 60;
        minutes = minutes % 60;
        
        if (hours > 0) {
            return String.format("%d hours, %d minutes, %d seconds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d minutes, %d seconds", minutes, seconds);
        } else {
            return String.format("%d seconds", seconds);
        }
    }
    
    /**
     * Clear all sessions (called on shutdown)
     */
    public void clearAll() {
        sessionStartTimes.clear();
    }
}
