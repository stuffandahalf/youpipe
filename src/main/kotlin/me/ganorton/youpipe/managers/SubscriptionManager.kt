// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.managers

import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import me.ganorton.youpipe.DataManager

public object SubscriptionManager : DataManager<Subscriptions>("config/subscriptions.json") {
	override fun mkInitData(): Subscriptions {
		println("SubscriptionHandler::initData")
		return Subscriptions("1.0.0", 0, ArrayList<Subscription>())
	}

	public override fun serialize(data: Subscriptions): String =
		Json.encodeToString(data)

	public override fun deserialize(source: String): Subscriptions =
		Json.decodeFromString<Subscriptions>(source)
}

@Serializable
public data class Subscription(val service_id: Int, val url: String, val name: String)

@Serializable
public data class Subscriptions(val app_version: String, val app_version_int: Int, val subscriptions: List<Subscription>)

