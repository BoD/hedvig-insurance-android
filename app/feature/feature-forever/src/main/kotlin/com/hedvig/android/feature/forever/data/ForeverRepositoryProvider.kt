package com.hedvig.android.feature.forever.data

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider

internal class ForeverRepositoryProvider(
  override val demoManager: DemoManager,
  override val demoImpl: ForeverRepository,
  override val prodImpl: ForeverRepository,
) : ProdOrDemoProvider<ForeverRepository>
