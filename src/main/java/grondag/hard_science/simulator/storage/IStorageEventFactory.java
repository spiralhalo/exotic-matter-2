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
package grondag.hard_science.simulator.storage;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

public interface IStorageEventFactory<T extends StorageType<T>> {
    public void postBeforeStorageDisconnect(IResourceContainer<T> storage);

    public void postAfterStorageConnect(IResourceContainer<T> storage);

    public void postStoredUpdate(IResourceContainer<T> storage, IResource<T> resource, long delta, @Nullable NewProcurementTask<T> request);

    public void postAvailableUpdate(IResourceContainer<T> storage, IResource<T> resource, long delta, @Nullable NewProcurementTask<T> request);

    public void postCapacityChange(IResourceContainer<T> storage, long delta);
}
