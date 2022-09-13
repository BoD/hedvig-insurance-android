package com.hedvig.android.notification.badge.data.crosssell.bottomnav

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.apollo.graphql.type.TypeOfContract
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.test.FakeFeatureManager
import com.hedvig.android.notification.badge.data.crosssell.CrossSellNotificationBadgeService
import com.hedvig.android.notification.badge.data.crosssell.FakeNotificationBadgeService
import com.hedvig.android.notification.badge.data.crosssell.GetCrossSellsContractTypesUseCase
import com.hedvig.android.notification.badge.data.crosssell.card.FakeGetCrossSellsContractTypesUseCase
import com.hedvig.android.notification.badge.data.referrals.ReferralsNotificationBadgeService
import com.hedvig.android.notification.badge.data.storage.NotificationBadge
import com.hedvig.android.notification.badge.data.storage.NotificationBadgeService
import com.hedvig.android.notification.badge.data.tab.BottomNavTab
import com.hedvig.android.notification.badge.data.tab.TabNotificationBadgeService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TabNotificationBadgeServiceTest {

  private fun tabNotificationBadgeService(
    notificationBadgeService: NotificationBadgeService,
    getCrossSellsContractTypesUseCase: GetCrossSellsContractTypesUseCase,
    isReferralCampaignOn: Boolean,
  ): TabNotificationBadgeService {
    return TabNotificationBadgeService(
      CrossSellBottomNavNotificationBadgeService(
        CrossSellNotificationBadgeService(
          getCrossSellsContractTypesUseCase,
          notificationBadgeService,
        ),
      ),
      ReferralsNotificationBadgeService(
        notificationBadgeService,
        FakeFeatureManager({ mapOf(Feature.REFERRAL_CAMPAIGN to isReferralCampaignOn) }),
      ),
    )
  }

  @Test
  fun `When backend returns no cross sells and the referral campaign is off, show no badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeService(this)
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellsContractTypesUseCase()
    val service = tabNotificationBadgeService(
      notificationBadgeService = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
      isReferralCampaignOn = false,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(emptySet())
  }

  @Test
  fun `When backend returns no cross sells and the referral campaign is on, show referral badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeService(this)
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellsContractTypesUseCase()
    val service = tabNotificationBadgeService(
      notificationBadgeService = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
      isReferralCampaignOn = true,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(setOf(BottomNavTab.REFERRALS))
  }

  @Test
  fun `When backend returns a cross sell and it's not seen, show insurance badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeService(this)
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellsContractTypesUseCase {
      setOf(TypeOfContract.SE_ACCIDENT)
    }
    val service = tabNotificationBadgeService(
      notificationBadgeService = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
      isReferralCampaignOn = false,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(setOf(BottomNavTab.INSURANCE))
  }

  @Test
  fun `When backend returns a cross sell but it's seen, show no badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeService(this).apply {
      setData(
        mapOf(NotificationBadge.BottomNav.CrossSellOnInsuranceScreen to setOf(TypeOfContract.SE_ACCIDENT.rawValue)),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellsContractTypesUseCase {
      setOf(TypeOfContract.SE_ACCIDENT)
    }
    val service = tabNotificationBadgeService(
      notificationBadgeService = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
      isReferralCampaignOn = false,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(emptySet())
  }

  @Test
  fun `When backend returns two cross sells but they're both seen, show no badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeService(this).apply {
      setData(
        mapOf(
          NotificationBadge.BottomNav.CrossSellOnInsuranceScreen to setOf(
            TypeOfContract.SE_ACCIDENT.rawValue,
            TypeOfContract.SE_CAR_FULL.rawValue,
          ),
        ),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellsContractTypesUseCase {
      setOf(TypeOfContract.SE_ACCIDENT, TypeOfContract.SE_CAR_FULL)
    }
    val service = tabNotificationBadgeService(
      notificationBadgeService = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
      isReferralCampaignOn = false,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(emptySet())
  }

  @Test
  fun `When backend returns two cross sells but only one is seen, show insurance badge`() = runTest {
    val notificationBadgeService = FakeNotificationBadgeService(this).apply {
      setData(
        mapOf(
          NotificationBadge.BottomNav.CrossSellOnInsuranceScreen to setOf(
            TypeOfContract.SE_ACCIDENT.rawValue,
          ),
        ),
      )
    }
    val getCrossSellsContractTypesUseCase = FakeGetCrossSellsContractTypesUseCase {
      setOf(TypeOfContract.SE_ACCIDENT, TypeOfContract.SE_CAR_FULL)
    }
    val service = tabNotificationBadgeService(
      notificationBadgeService = notificationBadgeService,
      getCrossSellsContractTypesUseCase = getCrossSellsContractTypesUseCase,
      isReferralCampaignOn = false,
    )

    val unseenBadges = service.unseenTabNotificationBadges().first()

    assertThat(unseenBadges).isEqualTo(setOf(BottomNavTab.INSURANCE))
  }
}
