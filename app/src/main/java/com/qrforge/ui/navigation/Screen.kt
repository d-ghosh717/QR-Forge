package com.qrforge.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Templates : Screen("templates")
    data object History : Screen("history")
    data object Settings : Screen("settings")

    // Create Flow
    data object ChooseType : Screen("create/choose_type")
    data object EnterData : Screen("create/enter_data/{qrType}?templateId={templateId}") {
        fun createRoute(qrType: String) = "create/enter_data/$qrType"
        fun createRoute(qrType: String, templateId: Long) = "create/enter_data/$qrType?templateId=$templateId"
    }
    data object Customize : Screen("create/customize/{qrType}/{historyId}?templateId={templateId}") {
        fun createRoute(qrType: String, historyId: Long) = "create/customize/$qrType/$historyId"
        fun createRoute(qrType: String, historyId: Long, templateId: Long) = "create/customize/$qrType/$historyId?templateId=$templateId"
    }
    data object QrResult : Screen("create/result/{historyId}") {
        fun createRoute(historyId: Long) = "create/result/$historyId"
    }

    // Detail
    data object QrDetail : Screen("detail/{historyId}") {
        fun createRoute(historyId: Long) = "detail/$historyId"
    }
    data object TemplatePreview : Screen("template/{templateId}") {
        fun createRoute(templateId: Long) = "template/$templateId"
    }
}
