package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.entity.AppConfigEntity
import com.example.data.repository.PakAdsRepository
import com.example.data.session.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: PakAdsRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PakAdsRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("PAK ADS", appName)
    }

    @Test
    fun `admin passcode is 085213`() {
        assertTrue(SessionManager.verifyAdminPassword("085213"))
        assertFalse(SessionManager.verifyAdminPassword("123456"))
    }

    @Test
    fun `user registration receives 100 PKR welcome bonus`() = runBlocking {
        val result = repository.registerUser("03001234567", "secret123")
        assertTrue(result.isSuccess)
        val userId = result.getOrNull()!!

        val user = repository.getUserSync(userId)
        assertNotNull(user)
        assertEquals("03001234567", user!!.mobileNumber)
        assertEquals(100.0, user.availableBalance, 0.01)
        assertEquals(100.0, user.totalEarnings, 0.01)
        assertTrue(user.welcomeBonusClaimed)
        assertEquals("NONE", user.depositStatus)
    }

    @Test
    fun `prevent duplicate mobile number registration`() = runBlocking {
        val res1 = repository.registerUser("03119876543", "pass1")
        assertTrue(res1.isSuccess)

        val res2 = repository.registerUser("03119876543", "pass2")
        assertTrue(res2.isFailure)
    }

    @Test
    fun `ads locked without approved deposit`() = runBlocking {
        val res = repository.registerUser("03221112233", "pass123")
        val userId = res.getOrNull()!!

        val adResult = repository.completeRewardedAd(userId)
        assertTrue(adResult.isFailure)
        assertTrue(adResult.exceptionOrNull()!!.message!!.contains("Please make a deposit first"))
    }

    @Test
    fun `approved deposit activates watch and earn and allows earning up to daily limit`() = runBlocking {
        val res = repository.registerUser("03334445566", "pass123")
        val userId = res.getOrNull()!!

        // Submit deposit of 300 PKR
        val depRes = repository.submitDeposit(
            userId = userId,
            amount = 300.0,
            senderName = "Test User",
            senderMobile = "03334445566",
            trxReference = "TRX-12345678",
            screenshotUri = "mock_receipt"
        )
        assertTrue(depRes.isSuccess)
        val depositId = depRes.getOrNull()!!.id

        // Admin approves deposit
        val reviewRes = repository.reviewDeposit(depositId, "APPROVED", null)
        assertTrue(reviewRes.isSuccess)

        val userAfterDeposit = repository.getUserSync(userId)!!
        assertEquals("APPROVED", userAfterDeposit.depositStatus)
        // 100 welcome bonus + 300 deposit = 400 PKR
        assertEquals(400.0, userAfterDeposit.availableBalance, 0.01)

        // User can now watch up to 5 ads (50 PKR each)
        for (i in 1..5) {
            val adRes = repository.completeRewardedAd(userId)
            assertTrue("Ad #$i should succeed", adRes.isSuccess)
            assertEquals(50.0, adRes.getOrNull()!!, 0.01)
        }

        val userAfter5Ads = repository.getUserSync(userId)!!
        assertEquals(5, userAfter5Ads.adsWatchedToday)
        assertEquals(250.0, userAfter5Ads.todayEarnings, 0.01)
        // 400 + 250 = 650 PKR
        assertEquals(650.0, userAfter5Ads.availableBalance, 0.01)

        // 6th ad should be blocked by daily limit
        val ad6Res = repository.completeRewardedAd(userId)
        assertTrue(ad6Res.isFailure)
        assertTrue(ad6Res.exceptionOrNull()!!.message!!.contains("Daily ad limit reached"))
    }

    @Test
    fun `minimum withdrawal validation and balance deduction`() = runBlocking {
        val res = repository.registerUser("03445556677", "pass123")
        val userId = res.getOrNull()!!

        // Attempt withdrawal below 800 PKR
        val failRes = repository.submitWithdrawal(
            userId = userId,
            method = "JazzCash",
            holderName = "Test Account",
            accountNumber = "03445556677",
            amount = 500.0,
            saveDetails = true
        )
        assertTrue(failRes.isFailure)
        assertTrue(failRes.exceptionOrNull()!!.message!!.contains("Minimum withdrawal"))

        // Add funds to reach 1000 PKR
        repository.manualAdjustBalance(userId, 900.0, "Test funds")
        val userWithFunds = repository.getUserSync(userId)!!
        assertEquals(1000.0, userWithFunds.availableBalance, 0.01)

        // Submit 800 PKR withdrawal
        val successWithdrawal = repository.submitWithdrawal(
            userId = userId,
            method = "JazzCash",
            holderName = "Test Account",
            accountNumber = "03445556677",
            amount = 800.0,
            saveDetails = true
        )
        assertTrue(successWithdrawal.isSuccess)

        val userAfterWithdraw = repository.getUserSync(userId)!!
        assertEquals(200.0, userAfterWithdraw.availableBalance, 0.01)
    }

    @Test
    fun `room database tracks deposits, withdrawals, and ad rewards accurately`() = runBlocking {
        val res = repository.registerUser("03556667788", "pass123")
        val userId = res.getOrNull()!!

        // 1. Initial Welcome Bonus transaction exists
        val initialTrxs = repository.getUserTransactions(userId).first()
        assertEquals(1, initialTrxs.size)
        assertEquals("WELCOME_BONUS", initialTrxs[0].type)
        assertEquals(100.0, initialTrxs[0].amount, 0.01)

        // 2. Track Deposit in Room DB
        val depRes = repository.submitDeposit(
            userId = userId,
            amount = 500.0,
            senderName = "Ali Khan",
            senderMobile = "03556667788",
            trxReference = "DEP-998877",
            screenshotUri = null
        )
        assertTrue(depRes.isSuccess)
        val depositId = depRes.getOrNull()!!.id

        // Check deposit transactions in Room
        val depCount = repository.getUserDepositsCount(userId).first()
        assertEquals(1, depCount)

        // Approve Deposit
        repository.reviewDeposit(depositId, "APPROVED", "Approved by Admin")
        val approvedDepositsTotal = repository.getUserTotalApprovedDeposits(userId).first()
        assertEquals(500.0, approvedDepositsTotal, 0.01)

        // 3. Track Ad Viewing Rewards in Room DB
        // Watch 3 ads (50 PKR each)
        for (i in 1..3) {
            repository.completeRewardedAd(userId)
        }
        val adRewardsTotal = repository.getUserTotalAdRewards(userId).first()
        assertEquals(150.0, adRewardsTotal, 0.01)
        val adsCount = repository.getUserAdsCompletedCount(userId).first()
        assertEquals(3, adsCount)

        // 4. Track Withdrawal in Room DB
        // Current balance: 100 + 500 + 150 = 750. Add 100 to meet 800 min withdrawal
        repository.manualAdjustBalance(userId, 100.0, "Adjustment")
        val withdrawRes = repository.submitWithdrawal(
            userId = userId,
            method = "Easypaisa",
            holderName = "Ali Khan",
            accountNumber = "03556667788",
            amount = 800.0,
            saveDetails = true
        )
        assertTrue(withdrawRes.isSuccess)

        val withdrawalsTotal = repository.getUserTotalWithdrawals(userId).first()
        assertEquals(800.0, withdrawalsTotal, 0.01)
        val withdrawalsCount = repository.getUserWithdrawalsCount(userId).first()
        assertEquals(1, withdrawalsCount)

        // 5. Verify Room search and category filters
        val adTrxs = repository.getUserTransactionsByType(userId, "AD_REWARD").first()
        assertEquals(3, adTrxs.size)

        val searchedTrxs = repository.searchUserTransactions(userId, "DEP-998877").first()
        assertEquals(1, searchedTrxs.size)
        assertEquals("DEPOSIT", searchedTrxs[0].type)
    }
}
