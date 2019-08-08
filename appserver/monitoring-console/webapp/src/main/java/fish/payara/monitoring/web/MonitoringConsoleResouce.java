/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2019 Payara Foundation and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://github.com/payara/Payara/blob/master/LICENSE.txt
 * See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * The Payara Foundation designates this particular file as subject to the "Classpath"
 * exception as provided by the Payara Foundation in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package fish.payara.monitoring.web;

import static java.util.stream.StreamSupport.stream;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.glassfish.internal.api.Globals;

import fish.payara.monitoring.model.Series;
import fish.payara.monitoring.store.MonitoringDataRepository;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class MonitoringConsoleResouce {

    private static MonitoringDataRepository getDataStore() {
        return Globals.getDefaultBaseServiceLocator().getService(MonitoringDataRepository.class);
    }

    @GET
    @Path("/series/{series}/statistics")
    public SeriesStatistics[] getSeriesStatistics(@PathParam("series") String series) {
        return SeriesStatistics.from(getDataStore().selectSeries(new Series(series)));
    }

    @GET
    @Path("/series/statistics/")
    public List<SeriesStatistics[]> querySeriesStatistics(@QueryParam("q") String query) {
        List<SeriesStatistics[]> matches = new ArrayList<>();
        for (String series : query.split("|")) {
            matches.add(SeriesStatistics.from(getDataStore().selectSeries(new Series(series))));
        }
        return matches;
    }

    @GET
    @Path("/series/")
    public String[] getSeriesNames() {
        return stream(getDataStore().selectAllSeries().spliterator(), false)
                .map(dataset -> dataset.getSeries().toString()).sorted().toArray(String[]::new);
    }
    
    //TODO add a method that returns the HTML needed to embedd one or more charts
}
