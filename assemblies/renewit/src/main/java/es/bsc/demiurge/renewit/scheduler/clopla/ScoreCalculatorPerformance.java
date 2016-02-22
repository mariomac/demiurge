package es.bsc.demiurge.renewit.scheduler.clopla;

import es.bsc.demiurge.core.clopla.domain.ClusterState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class ScoreCalculatorPerformance implements SimpleScoreCalculator<ClusterState> {
    private Logger logger = LogManager.getLogger(ScoreCalculatorPerformance.class);

    @Override
    public Score calculateScore(ClusterState clusterState) {

        clusterState.getHosts();


        return null;
    }
}
