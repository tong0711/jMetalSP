package org.uma.jmetalsp.examples.dynamictsp;import org.apache.kafka.common.serialization.IntegerDeserializer;import org.uma.jmetal.operator.CrossoverOperator;import org.uma.jmetal.operator.MutationOperator;import org.uma.jmetal.operator.SelectionOperator;import org.uma.jmetal.operator.impl.crossover.PMXCrossover;import org.uma.jmetal.operator.impl.mutation.PermutationSwapMutation;import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;import org.uma.jmetal.solution.PermutationSolution;import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;import org.uma.jmetalsp.*;import org.uma.jmetalsp.algorithm.nsgaii.DynamicNSGAIIBuilder;import org.uma.jmetalsp.consumer.ChartConsumer;import org.uma.jmetalsp.consumer.LocalDirectoryOutputConsumer;import org.uma.jmetalsp.observeddata.AlgorithmObservedData;import org.uma.jmetalsp.observeddata.ObservedValue;import org.uma.jmetalsp.observer.impl.DefaultObservable;import org.uma.jmetalsp.problem.tsp.MultiobjectiveTSPBuilderFromNYData;import org.uma.jmetalsp.problem.tsp.MultiobjectiveTSPBuilderFromTSPLIBFiles;import org.uma.jmetalsp.problem.tsp.TSPMatrixData;import org.uma.jmetalsp.spark.SparkRuntime;import org.uma.jmetalsp.spark.streamingdatasource.SimpleSparkStructuredKafkaStreamingTSP;import org.uma.jmetalsp.streamingdatasource.SimpleKafkaStreamingTSPDataSource;import java.io.IOException;import java.util.HashMap;import java.util.List;import java.util.Map;/** * Example of SparkSP application. * Features: * - Algorithm: to choose among NSGA-II and MOCell * - Problem: Bi-objective TSP * - Default streaming runtime (Spark is not used) * * @author Antonio J. Nebro <antonio@lcc.uma.es> */public class DynamicTSPApplicationKafkaStreaming {  public static void main(String[] args) throws IOException, InterruptedException {    // STEP 1. Create the problem    DynamicProblem<PermutationSolution<Integer>, ObservedValue<TSPMatrixData>> problem;    //problem = new MultiobjectiveTSPBuilderFromTSPLIBFiles("data/kroA100.tsp", "data/kroB100.tsp")     //       .build();    problem = new MultiobjectiveTSPBuilderFromNYData("data/nyData.txt").build() ;    // STEP 2. Create the algorithm    CrossoverOperator<PermutationSolution<Integer>> crossover;    MutationOperator<PermutationSolution<Integer>> mutation;    SelectionOperator<List<PermutationSolution<Integer>>, PermutationSolution<Integer>> selection;    crossover = new PMXCrossover(0.9);    double mutationProbability = 0.2;    mutation = new PermutationSwapMutation<Integer>(mutationProbability);    selection = new BinaryTournamentSelection<>(            new RankingAndCrowdingDistanceComparator<PermutationSolution<Integer>>());    DynamicAlgorithm<List<PermutationSolution<Integer>>, AlgorithmObservedData> algorithm;    algorithm = new DynamicNSGAIIBuilder<>(crossover, mutation, new DefaultObservable<>())            .setMaxEvaluations(25000)            .setPopulationSize(100)            .setSelectionOperator(selection)            .build(problem);    // STEP 3. Create the streaming data source and register the problem    String topic="tsp";    Map<String,Object> kafkaParams = new HashMap<>();    kafkaParams.put("bootstrap.servers", "localhost:9092");    kafkaParams.put("key.deserializer", IntegerDeserializer.class);    kafkaParams.put("value.deserializer", IntegerDeserializer.class);    kafkaParams.put("group.id", "use_a_separate_group_id_for_each_stream");    kafkaParams.put("auto.offset.reset", "latest");    kafkaParams.put("enable.auto.commit", false);    SimpleKafkaStreamingTSPDataSource streamingTSPSource = new SimpleKafkaStreamingTSPDataSource();    streamingTSPSource.setTopic(topic);    //streamingTSPSource.getObservable().register(problem);    // STEP 4. Create the data consumers and register into the algorithm    DataConsumer<AlgorithmObservedData> localDirectoryOutputConsumer =            new LocalDirectoryOutputConsumer<PermutationSolution<Integer>>("outputdirectory");    DataConsumer<AlgorithmObservedData> chartConsumer =            new ChartConsumer<PermutationSolution<Integer>>(algorithm.getName());   // algorithm.getObservable().register(localDirectoryOutputConsumer);    //algorithm.getObservable().register(chartConsumer);    // STEP 5. Create the application and run    JMetalSPApplication<            PermutationSolution<Integer>,            DynamicProblem<PermutationSolution<Integer>, ObservedValue<TSPMatrixData>>,            DynamicAlgorithm<List<PermutationSolution<Integer>>, AlgorithmObservedData>> application;    application = new JMetalSPApplication<>();    application.setStreamingRuntime(new KafkaRuntime("tsp"))            .setProblem(problem)            .setAlgorithm(algorithm)            .addStreamingDataSource(streamingTSPSource,problem)            .addAlgorithmDataConsumer(localDirectoryOutputConsumer)            .addAlgorithmDataConsumer(chartConsumer)            .run();  }}