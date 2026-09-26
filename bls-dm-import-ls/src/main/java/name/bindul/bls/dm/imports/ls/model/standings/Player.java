/*
 * Copyright (c) 2026. Bindul Bhowmik
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
package name.bindul.bls.dm.imports.ls.model.standings;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data @RequiredArgsConstructor
public class Player {

    private final PlayerInfo playerInfo;
    private int avg;
    private int hdcp;
    private int games;
    private int scratchPins;
    private int highScratchGame;
    private int highScratchSeries;
    private int avgHdcpGame;
    private int avgHdcpSeries;
    private int lastWeekGame1;
    private int lastWeekGame2;
    private int lastWeekGame3;
    private int lastWeekScratchTotal;
    private int lastWeekHdcpTotal;

}
