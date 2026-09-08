package com.zenith.focus.domain.repository

import com.zenith.focus.domain.model.LockMode
import com.zenith.focus.domain.model.LockState
import kotlinx.coroutines.flow.StateFlow

interface LockRepository {
    val lockState: StateFlow<LockState>
    suspend fun startLock(durationMillis: Long, mode: LockMode = LockMode.QUICK, label: String = "Focus Lock")
    suspend fun startLockUntil(targetTimestampMillis: Long, mode: LockMode, label: String)
    suspend fun endLock()
    suspend fun refreshLockState()
}
