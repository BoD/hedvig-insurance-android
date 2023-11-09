package com.hedvig.android.notification.badge.data.crosssell.bottomnav

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager
import com.hedvig.android.notification.badge.data.crosssell.CrossSellIdentifier
import com.hedvig.android.notification.badge.data.crosssell.CrossSellNotificationBadgeService
import com.hedvig.android.notification.badge.data.crosssell.FakeNotificationBadgeStorage
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellIdentifiersUseCase
import com.hedvig.android.notification.badge.data.crosssell.card.FakeGetCrossSellIdentifiersUseCase
import com.hedvig.android.notification.badge.data.referrals.ReferralsNotificationBadgeService
import com.hedvig.android.notification.badge.data.storage.NotificationBadge
import com.hedvig.android.notification.badge.data.storage.NotificationBadgeStorage
import com.hedvig.android.notification.badge.data.tab.BottomNavTab
import com.hedvig.android.notification.badge.data.tab.TabNotificationBadgeService
import giraffe.type.TypeOfContract
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TabNotificationBadgeServiceTest {
  private fun tabNotificationBadgeService(
    notificationBadgeStorage: NotificationBadgeStorage,
    getCrossSellIdentifiersUseCase: GetCrossSellIdentifiersUseCase,
    isReferralCampaignOn: Boolean,
  ): TabNotificationBadgeService {
    return TabNotificationBadgeService(
      CrossSellBottomNavNotificationBadgeService(
        CrossSellNotificationBadgeService(
          getCrossSellIdentifiersUseCase,
          notificationBadgeStorage,
        ),
      ),
      ReferralsNotificationBadgeService(
        notificationBadgeStorage,
        FakeFeatureManager({ mapOf(Feature.REFERRAL_CAMPAIGN to isReferralCampaignOn) }),
      ),
    )
  }

  @Test
  fun `When backend returns no cross sells and the referral campaign is off, show no badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeStorage(this)
    val getCrossSellsContractTypeIdentifiersUseCase = FakeGetCrossSellIdentifiersUseCase()
    val service = tabNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellIdentifiersUseCase = getCrossSellsContractTypeIdentifiersUseCase,
      isReferralCampaignOn = false,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(emptySet())
  }

  @Test
  fun `When backend returns no cross sells and the referral campaign is on, show referral badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeStorage(this)
    val getCrossSellsContractTypeIdentifiersUseCase = FakeGetCrossSellIdentifiersUseCase()
    val service = tabNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellIdentifiersUseCase = getCrossSellsContractTypeIdentifiersUseCase,
      isReferralCampaignOn = true,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(setOf(BottomNavTab.REFERRALS))
  }

  @Test
  fun `When backend returns a cross sell and it's not seen, show insurance badge`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val notificationBadgeService = FakeNotificationBadgeStorage(this)
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellIdentifiersUseCase {
      setOf(seAccident)
    }
    val service = tabNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellIdentifiersUseCase = getCrossSellsContractTypesUseCase,
      isReferralCampaignOn = false,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(setOf(BottomNavTab.INSURANCE))
  }

  @Test
  fun `When backend returns a cross sell but it's seen, show no badge`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val notificationBadgeService = FakeNotificationBadgeStorage(this).apply {
      setValue(
        NotificationBadge.BottomNav.CrossSellOnInsuranceScreen,
        setOf(seAccident.rawValue),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellIdentifiersUseCase {
      setOf(seAccident)
    }
    val service = tabNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellIdentifiersUseCase = getCrossSellsContractTypesUseCase,
      isReferralCampaignOn = false,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(emptySet())
  }

  @Test
  fun `When backend returns two cross sells but they're both seen, show no badge`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val seCarFull = CrossSellIdentifier("SE_CAR_FULL")
    val notificationBadgeService = FakeNotificationBadgeStorage(this).apply {
      setValue(
        NotificationBadge.BottomNav.CrossSellOnInsuranceScreen,
        setOf(
          seAccident.rawValue,
          seCarFull.rawValue,
        ),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellIdentifiersUseCase {
      setOf(seAccident, seCarFull)
    }
    val service = tabNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellIdentifiersUseCase = getCrossSellsContractTypesUseCase,
      isReferralCampaignOn = false,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(emptySet())
  }

  @Test
  fun `When backend returns two cross sells but only one is seen, still show insurance badge`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val seCarFull = CrossSellIdentifier("SE_CAR_FULL")
    val notificationBadgeService = FakeNotificationBadgeStorage(this).apply {
      setValue(
        NotificationBadge.BottomNav.CrossSellOnInsuranceScreen,
        setOf(seAccident.rawValue),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellIdentifiersUseCase {
      setOf(seAccident, seCarFull)
    }
    val service = tabNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellIdentifiersUseCase = getCrossSellsContractTypesUseCase,
      isReferralCampaignOn = false,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(setOf(BottomNavTab.INSURANCE))
  }

  @Test
  fun `Storing old seen contract types shouldn't affect the shown badge`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val seCarFull = CrossSellIdentifier("SE_CAR_FULL")
    val seQasaShortTermRental = CrossSellIdentifier("SE_QASA_SHORT_TERM_RENTAL")
    val notificationBadgeService = FakeNotificationBadgeStorage(this).apply {
      setValue(
        NotificationBadge.BottomNav.CrossSellOnInsuranceScreen,
        setOf(
          seAccident.rawValue,
          TypeOfContract.SE_APARTMENT_BRF.rawValue,
          seCarFull.rawValue,
          TypeOfContract.SE_HOUSE.rawValue,
        ),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellIdentifiersUseCase {
      setOf(seQasaShortTermRental)
    }
    val service = tabNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellIdentifiersUseCase = getCrossSellsContractTypesUseCase,
      isReferralCampaignOn = false,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(setOf(BottomNavTab.INSURANCE))
  }

  @Test
  fun `When a notification is shown, when it is marked as seen it no longer shows`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val notificationBadgeService = FakeNotificationBadgeStorage(this)
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellIdentifiersUseCase {
      setOf(seAccident)
    }
    val service = tabNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellIdentifiersUseCase = getCrossSellsContractTypesUseCase,
      isReferralCampaignOn = false,
    )
    service.unseenTabNotificationBadges().test {
      assertThat(awaitItem()).isEqualTo(setOf(BottomNavTab.INSURANCE))
      service.visitTab(BottomNavTab.INSURANCE)
      assertThat(awaitItem()).isEqualTo(emptySet())
      ensureAllEventsConsumed()
    }
  }

  @Test
  fun `When two notifications are shown, they get cleared one by one when visiting the tabs`() = runTest {
    val seAccident = CrossSellIdentifier("SE_ACCIDENT")
    val notificationBadgeService = FakeNotificationBadgeStorage(this)
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellIdentifiersUseCase {
      setOf(seAccident)
    }
    val service = tabNotificationBadgeService(
      notificationBadgeStorage = notificationBadgeService,
      getCrossSellIdentifiersUseCase = getCrossSellsContractTypesUseCase,
      isReferralCampaignOn = true,
    )

    service.unseenTabNotificationBadges().test {
      assertThat(awaitItem()).isEqualTo(setOf(BottomNavTab.INSURANCE, BottomNavTab.REFERRALS))
      service.visitTab(BottomNavTab.INSURANCE)
      assertThat(awaitItem()).isEqualTo(setOf(BottomNavTab.REFERRALS))
      service.visitTab(BottomNavTab.REFERRALS)
      assertThat(awaitItem()).isEqualTo(emptySet())
      ensureAllEventsConsumed()
    }
  }
}
