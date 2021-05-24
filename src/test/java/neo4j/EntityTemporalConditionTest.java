package neo4j;

import benchmark.client.DBProxy;
import benchmark.client.neo4j.Neo4jExecutorClient;
import benchmark.transaction.definition.EntityTemporalConditionTx;
import benchmark.transaction.generation.BenchmarkTxResultProcessor;
import benchmark.utils.Helper;
import com.aliyun.openservices.aliyun.log.producer.Producer;
import com.aliyun.openservices.aliyun.log.producer.errors.ProducerException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class EntityTemporalConditionTest {
    private static int threadCnt = Integer.parseInt(Helper.mustEnv("MAX_CONNECTION_CNT")); // number of threads to send queries.
    private static String serverHost = Helper.mustEnv("DB_HOST");
    private static String resultFile = Helper.mustEnv("SERVER_RESULT_FILE");

    private static Producer logger;
    private static DBProxy client;
    private static BenchmarkTxResultProcessor post;

    @BeforeClass
    public static void init() throws IOException, ExecutionException, InterruptedException, SQLException, ClassNotFoundException {
        client = new Neo4jExecutorClient(serverHost, threadCnt, 800);
        post = new BenchmarkTxResultProcessor("Neo4j1(EntityTemporalConditionTest)", Helper.codeGitVersion());
        logger = Helper.getLogger();
        post.setLogger(logger);
        post.setResult(new File(resultFile));
    }

    @Test
    public void test() throws Exception {
        for (int i = 0; i < 200; ++i)
            query("travel_time", Helper.timeStr2int("201006300830"), Helper.timeStr2int("201006300930"), 600, 100000);
    }

    private void query(String propertyName, int st, int en, int vMin, int vMax) throws Exception {
        EntityTemporalConditionTx tx = new EntityTemporalConditionTx();
        tx.setP(propertyName);
        tx.setT0(st);
        tx.setT1(en);
        tx.setVMin(vMin);
        tx.setVMax(vMax);
        post.process(client.execute(tx), tx);
    }

    @AfterClass
    public static void close() throws IOException, InterruptedException, ProducerException {
        client.close();
        Thread.sleep(1000 * 60 * 2);
        post.close();
        logger.close();
    }

}
