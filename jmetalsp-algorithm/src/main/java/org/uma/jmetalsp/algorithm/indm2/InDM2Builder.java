package org.uma.jmetalsp.algorithm.indm2;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetalsp.DynamicProblem;
import org.uma.jmetalsp.InteractiveAlgorithm;
import org.uma.jmetalsp.observeddata.AlgorithmObservedData;
import org.uma.jmetalsp.observer.Observable;

import java.util.List;

/**
 * @author Cristobal Barba <cbarba@lcc.uma.es>
 */
public class InDM2Builder<
        S extends Solution<?>,
        P extends DynamicProblem<S, ?>> {


  private Observable<AlgorithmObservedData> observable ;
  private InteractiveAlgorithm<S,List<S>> interactiveAlgorithm;

  private int maxIterations;
  private int populationSize;

  public InDM2Builder(InteractiveAlgorithm<S,List<S>> interactiveAlgorithm,
                      Observable<AlgorithmObservedData> observable) {

    this.interactiveAlgorithm = interactiveAlgorithm;
    this.maxIterations = 25000;
    this.populationSize = 100;
    this.observable = observable;

  }


  public InDM2Builder<S, P> setObservable(
      Observable<AlgorithmObservedData> observable) {
    this.observable = observable;
    return this;
  }

  public InDM2Builder<S, P> setInteractiveAlgorithm(
      InteractiveAlgorithm<S, List<S>> interactiveAlgorithm) {
    this.interactiveAlgorithm = interactiveAlgorithm;
    return this;
  }

  public InDM2Builder<S, P> setMaxIterations(int maxIterations) {
    this.maxIterations = maxIterations;
    return this;
  }

  public InDM2Builder<S, P> setPopulationSize(int populationSize) {
    this.populationSize = populationSize;
    return this;
  }



  public InDM2<S> build(P problem) {

    /**
     * Problem<S> problem, int populationSize, int maxEvaluations,InteractiveAlgorithm<S,List<S>> interactiveAlgorithm,
     *                Observable<AlgorithmObservedData<S>> observable
     */
    return new InDM2(problem, populationSize, maxIterations, interactiveAlgorithm, observable);

  }
}
