/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.networking.test.channeltest;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

final class ChannelScreen extends Screen {
	private ButtonWidget s2cButton;
	private ButtonWidget c2sButton;
	private ChannelList channelList;

	ChannelScreen(NetworkingChannelClientTest mod) {
		super(Text.literal("TODO"));
	}

	@Override
	protected void init() {
		this.s2cButton = this.addDrawableSelectableElement(ButtonWidget.builder(Text.literal("S2C"), this::toS2C)
			.positionAndSize(this.width / 2 - 55, 5, 50, 20)
			.tooltip(Tooltip.create(Text.literal("Packets this client can receive")))
			.build());
		this.c2sButton = this.addDrawableSelectableElement(ButtonWidget.builder(Text.literal("C2S"), this::toC2S)
			.positionAndSize(this.width / 2 + 5, 5, 50, 20)
			.tooltip(Tooltip.create(Text.literal("Packets the server can receive")))
			.build());
		this.addDrawableSelectableElement(ButtonWidget.builder(Text.literal("Close"), button -> this.closeScreen())
			.positionAndSize(this.width / 2 - 60, this.height - 25, 120, 20)
			.build());
		this.channelList = this.addDrawable(new ChannelList(this.client, this.width, this.height - 60, 30, this.height - 30, this.textRenderer.fontHeight + 2));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		this.renderBackground(graphics, mouseX, mouseY, delta);
		this.channelList.render(graphics, mouseX, mouseY, delta);
		super.render(graphics, mouseX, mouseY, delta);

		if (this.s2cButton.active && this.c2sButton.active) {
			final var clickMe = Text.of("Click S2C or C2S to view supported channels");

			final int textWidth = this.textRenderer.getWidth(clickMe);
			//noinspection ConstantConditions
			graphics.drawText(
				this.textRenderer,
				clickMe,
				(int) (this.width / 2.0F - (textWidth / 2.0F)),
				60,
				Formatting.YELLOW.getColorValue(),
				false
			);
		}
	}

	void refresh() {
		if (!this.c2sButton.active && this.s2cButton.active) {
			this.toC2S(this.c2sButton);
		}
	}

	private void toC2S(ButtonWidget button) {
		this.s2cButton.active = true;
		button.active = false;
		this.channelList.clear();

		for (CustomPayload.Id<?> receiver : ClientPlayNetworking.getSendable()) {
			this.channelList.addEntry(this.channelList.new Entry(receiver.id()));
		}
	}

	private void toS2C(ButtonWidget button) {
		this.c2sButton.active = true;
		button.active = false;
		this.channelList.clear();

		for (CustomPayload.Id<?> receiver : ClientPlayNetworking.getReceived()) {
			this.channelList.addEntry(this.channelList.new Entry(receiver.id()));
		}
	}
}
