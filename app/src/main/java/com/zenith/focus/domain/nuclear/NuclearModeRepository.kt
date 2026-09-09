package com.zenith.focus.domain.nuclear

import kotlinx.coroutines.flow.StateFlow

interface NuclearModeRepository {
    /**
     * Authoritative reactive stream of the Nuclear Session state.
     */
    val session: StateFlow<NuclearSession>

    /**
     * Step 1: Prepares an arming session for user confirmation.
     * State moves to ARMING.
     */
    suspend fun armSession(
        durationMillis: Long,
        currentBlockedCount: Int,
        enabledCategories: Set<com.zenith.focus.domain.model.ContentCategory> = com.zenith.focus.domain.model.ContentCategory.values().toSet()
    )

    /**
     * Step 2: Irreversibly locks in the session after explicit Hold-To-Activate confirmation.
     * State moves to ACTIVE. Once active, it CANNOT be cancelled or edited.
     */
    suspend fun activateArmedSession(): Boolean

    /**
     * Cancels the ARMING phase if user backs out before confirming.
     * Fails if session is already ACTIVE.
     */
    suspend fun cancelArming(): Boolean

    /**
     * Evaluates current timestamps and transitions to EXPIRED if timer has concluded.
     * Returns true if session is still active, false otherwise.
     */
    suspend fun checkAndUpdateExpiration(): Boolean

    /**
     * Acknowledges that the expired session has been viewed, transitioning back to INACTIVE.
     * Only permitted if status is EXPIRED.
     */
    suspend fun acknowledgeCompletedSession(): Boolean

    /**
     * Recalibrates monotonic reference after device reboot.
     */
    suspend fun onDeviceRebooted()
}
