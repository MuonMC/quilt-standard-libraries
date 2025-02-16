/*
 * Copyright 2021, 2022, 2023, 2024 The Quilt Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.networking.test.payload;

import java.util.List;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.networking.api.CustomPayloads;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;
import org.quiltmc.qsl.networking.impl.NetworkingImpl;
import org.quiltmc.qsl.networking.test.NetworkingTestMods;

// Test cannot run on a server, so only run with a client
public class C2SPayloadTest implements ClientModInitializer {
	private static boolean received = false;

	@Override
	public void onInitializeClient(ModContainer mod) {
		CustomPayloads.registerC2SPayload(TestC2SPayload.ID, TestC2SPayload::new);
		ServerPlayNetworking.registerGlobalReceiver(TestC2SPayload.ID, this::handleTestPayload);

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			sender.sendPayload(new TestC2SPayload(List.of("String"), 1, 1.0));

			if (NetworkingImpl.RESERIALIZE_CUSTOM_PAYLOADS) {
				var buf = PacketByteBufs.create();
				buf.writeCollection(List.of("a", "b"), PacketByteBuf::writeString);
				buf.writeInt(2);
				buf.writeDouble(2.0);
				sender.sendPacket(TestC2SPayload.ID, buf);
			}

			client.execute(() -> {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				if (!received) {
					throw new IllegalStateException("Did not receive C2S payload on the server.");
				}
			});
		});
	}

	public void handleTestPayload(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, TestC2SPayload payload, PacketSender<CustomPayload> responseSender) {
		NetworkingTestMods.LOGGER.info("Received C2S payload: {}", payload.toString());
		received = true;
	}

	public record TestC2SPayload(List<String> strings, int a, double b) implements CustomPayload {
		public static final Identifier ID = Identifier.of("quilt_networking_testmod", "test_c2s_payload");

		TestC2SPayload(PacketByteBuf buf) {
			this(buf.readList(PacketByteBuf::readString), buf.readInt(), buf.readDouble());
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeCollection(this.strings, PacketByteBuf::writeString);
			buf.writeInt(this.a);
			buf.writeDouble(this.b);
		}

		@Override
		public Identifier id() {
			return ID;
		}
	}
}
