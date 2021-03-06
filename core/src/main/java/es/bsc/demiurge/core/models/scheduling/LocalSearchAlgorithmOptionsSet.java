/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.demiurge.core.models.scheduling;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class LocalSearchAlgorithmOptionsSet extends LocalSearchAlgorithm {

    private Map<String, Integer> options;

    public LocalSearchAlgorithmOptionsSet(String name, Map<String, Integer> options) {
        super(name);
        this.options = options == null ? new HashMap<String,Integer>() : new HashMap<String,Integer>(options);
    }

    public Map<String, Integer> getOptions() {
        return Collections.unmodifiableMap(options);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
		sb.append("name = ").append(name).append(", options= {");
        for(Map.Entry<String,Integer> m : options.entrySet()) {
            sb.append(m.getKey()).append("->").append(m.getValue()).append(",");
        }
		sb.append("}");
        return sb.toString();
    }
}
