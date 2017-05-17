/*******************************************************************************
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.dkpro.tc.ml;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.core.task.ExtractFeaturesTask;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.core.task.InitTaskDeep;
import org.dkpro.tc.core.task.MetaInfoTask;
import org.dkpro.tc.core.task.deep.EmbeddingTask;
import org.dkpro.tc.core.task.deep.VectorizationTask;
import org.dkpro.tc.ml.report.TcTaskType;

/**
 * Train-Test setup
 */
public class DeepLearningExperimentTrainTest extends Experiment_ImplBase {

	protected InitTaskDeep initTaskTrain;
	protected InitTaskDeep initTaskTest;
	protected EmbeddingTask embeddingTask;
	protected VectorizationTask vectorizationTrainTask;
	protected VectorizationTask vectorizationTestTask;

	public DeepLearningExperimentTrainTest() {/* needed for Groovy */
	}

	/**
	 * Preconfigured train-test setup.
	 */
	public DeepLearningExperimentTrainTest(String aExperimentName, Class<? extends TCMachineLearningAdapter> mlAdapter)
			throws TextClassificationException {
		setExperimentName(aExperimentName);
		setMachineLearningAdapter(mlAdapter);
		// set name of overall batch task
		setType("Evaluation-" + experimentName);
		setAttribute(TC_TASK_TYPE, TcTaskType.EVALUATION.toString());
	}

	// TODO: Introduce new attributes or reuse existing ? Becomes confusing with
	// some being used for DL setups only

	/**
	 * Initializes the experiment. This is called automatically before
	 * execution. It's not done directly in the constructor, because we want to
	 * be able to use setters instead of the arguments in the constructor.
	 * 
	 */
	@Override
	protected void init() {
		if (experimentName == null)

		{
			throw new IllegalStateException("You must set an experiment name");
		}

		// init the train part of the experiment
		initTaskTrain = new InitTaskDeep();
		initTaskTrain.setPreprocessing(getPreprocessing());
		initTaskTrain.setOperativeViews(operativeViews);
		initTaskTrain.setTesting(false);
		initTaskTrain.setType(initTaskTrain.getType() + "-Train-" + experimentName);
		initTaskTrain.setAttribute(TC_TASK_TYPE, TcTaskType.INIT_TRAIN.toString());

		// init the test part of the experiment
		initTaskTest = new InitTaskDeep();
		initTaskTest.setTesting(true);
		initTaskTest.setPreprocessing(getPreprocessing());
		initTaskTest.setOperativeViews(operativeViews);
		initTaskTest.setType(initTaskTest.getType() + "-Test-" + experimentName);
		initTaskTest.setAttribute(TC_TASK_TYPE, TcTaskType.INIT_TEST.toString());

		// get some meta data depending on the whole document collection that we
		// need for training
		embeddingTask = new EmbeddingTask();
		embeddingTask.setOperativeViews(operativeViews);
		embeddingTask.setType(embeddingTask.getType() + "-" + experimentName);
		embeddingTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN, EmbeddingTask.INPUT_KEY_TRAIN);
		embeddingTask.addImport(initTaskTest, InitTask.OUTPUT_KEY_TEST, EmbeddingTask.INPUT_KEY_TEST);
		embeddingTask.setAttribute(TC_TASK_TYPE, TcTaskType.META.toString());

		// feature extraction on training data
		vectorizationTrainTask = new VectorizationTask();
		vectorizationTrainTask.setType(vectorizationTrainTask.getType() + "-Train-" + experimentName);
		vectorizationTrainTask.setTesting(false);
		vectorizationTrainTask.addImport(initTaskTrain, InitTask.OUTPUT_KEY_TRAIN, VectorizationTask.INPUT_KEY);
		vectorizationTrainTask.setAttribute(TC_TASK_TYPE, TcTaskType.FEATURE_EXTRACTION_TRAIN.toString());

		// feature extraction on test data
		vectorizationTestTask = new VectorizationTask();
		vectorizationTestTask.setType(vectorizationTestTask.getType() + "-Test-" + experimentName);
		vectorizationTestTask.setTesting(true);
		vectorizationTestTask.addImport(initTaskTest, InitTask.OUTPUT_KEY_TEST, VectorizationTask.INPUT_KEY);
		vectorizationTrainTask.setAttribute(TC_TASK_TYPE, TcTaskType.FEATURE_EXTRACTION_TEST.toString());

		// DKPro Lab issue 38: must be added as *first* task
		addTask(initTaskTrain);
		addTask(initTaskTest);
		addTask(embeddingTask);
		addTask(vectorizationTrainTask);
		addTask(vectorizationTestTask);
	}
}
