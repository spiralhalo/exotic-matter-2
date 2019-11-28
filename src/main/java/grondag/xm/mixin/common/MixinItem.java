/*******************************************************************************
 * Copyright 2019 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.xm.mixin.common;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.dispatch.XmItemAccess;

@Mixin(Item.class)
public class MixinItem implements XmItemAccess {
	private Function<ItemStack, MutableModelState> modelStateFunc = null;

	@Override
	public void xm_modelStateFunc(Function<ItemStack, MutableModelState> func) {
		modelStateFunc = func;
	}

	@Override
	public Function<ItemStack, MutableModelState> xm_modelStateFunc() {
		return modelStateFunc;
	}

}
