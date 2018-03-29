/*******************************************************************************
 * Copyright 2018
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
package org.dkpro.tc.core.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.uima.collection.CollectionReaderDescription;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;

/**
 * Convenience class that builds a parameter space object that can be passed to a DKPro Lab
 * experiment.
 */
public class ExperimentBuilder
    implements Constants
{
    List<TcShallowLearningAdapter> adapter = new ArrayList<>();
    List<String[]> arguments = new ArrayList<>();
    String learningMode = LM_SINGLE_LABEL;
    String featureMode = FM_DOCUMENT;
    Map<String, Object> readers = null;
    List<TcFeatureSet> featureSets = new ArrayList<>();

    /**
     * Adds a machine learning adapter configuration. Several configurations for the same adapter
     * can be added by calling this method multiple times with other parametritzation for the
     * adapter
     * 
     * @param adapter
     *            the adapter that shall be executed
     * @param arguments
     *            the parametrization of this adapter - optional
     */
    public void addAdapterConfiguration(TcShallowLearningAdapter adapter, String... arguments)
    {
        this.adapter.add(adapter);
        this.arguments.add(arguments);
    }

    /**
     * Wires all provided information into a parameter space object that can be provided to an
     * experiment
     * 
     * @return the parameter space filled with the provided information
     */
    public ParameterSpace build()
    {
        List<Dimension<?>> dimensions = new ArrayList<>();

        dimensions.add(getAsDimensionMachineLearningAdapter());
        dimensions.add(getAsDimensionFeatureMode());
        dimensions.add(getAsDimensionLearningMode());
        dimensions.add(getAsDimensionFeatureSets());
        dimensions.add(getAsDimensionReaders());

        ParameterSpace ps = new ParameterSpace();
        ps.setDimensions(dimensions.toArray(new Dimension<?>[0]));

        return ps;
    }

    private Dimension<?> getAsDimensionMachineLearningAdapter()
    {
        List<Map<String, Object>> adapterMaps = getAdapterInfo();

        @SuppressWarnings("unchecked")
        Map<String, Object>[] array = adapterMaps.toArray(new Map[0]);
        Dimension<Map<String, Object>> mlaDim = Dimension.createBundle("configurations", array);
        return mlaDim;
    }

    private Dimension<?> getAsDimensionReaders()
    {
        return Dimension.createBundle(DIM_READERS, readers);
    }

    private Dimension<?> getAsDimensionFeatureSets()
    {
        if (featureSets.isEmpty()) {
            throw new IllegalStateException(
                    "No feature sets provided, please provide at least one feature set i.e. ["
                            + TcFeatureSet.class.getName() + "]");
        }

        return Dimension.create(DIM_FEATURE_SET, featureSets.toArray(new TcFeatureSet[0]));
    }

    private Dimension<?> getAsDimensionLearningMode()
    {
        if (learningMode == null) {
            throw new NullPointerException(
                    "No learning mode set, please provide this information via the respective setter method");
        }

        return Dimension.create(DIM_LEARNING_MODE, learningMode);
    }

    private Dimension<?> getAsDimensionFeatureMode()
    {
        if (featureMode == null) {
            throw new NullPointerException(
                    "No feature mode set, please provide this information via the respective setter method");
        }

        return Dimension.create(DIM_FEATURE_MODE, featureMode);
    }

    private List<Map<String, Object>> getAdapterInfo()
    {
        if (adapter.size() == 0) {
            throw new IllegalStateException(
                    "No machine learning adapter set - Provide at least one machine learning configuration");
        }

        List<Map<String, Object>> maps = new ArrayList<>();

        for (int i = 0; i < adapter.size(); i++) {
            TcShallowLearningAdapter a = adapter.get(i);
            String[] strings = arguments.get(i);

            List<Object> o = new ArrayList<>();
            o.add(a);
            for (String s : strings) {
                o.add(s);
            }

            Map<String, Object> m = new HashedMap<>();
            m.put(DIM_CLASSIFICATION_ARGS, o);
            m.put(DIM_DATA_WRITER, a.getDataWriterClass().getName());
            m.put(DIM_FEATURE_USE_SPARSE, a.useSparseFeatures() + "");

            maps.add(m);
        }
        return maps;
    }

    /**
     * Sets the readers of an experiment. Overwrites any previously added readers.
     * 
     * @param dimReaders
     */
    public void setReaders(Map<String, Object> dimReaders) throws NullPointerException
    {
        nullCheckReaderMap(dimReaders);
        this.readers = dimReaders;
        sanityCheckReaders();
    }

    private void nullCheckReaderMap(Map<String, Object> readers)
    {
        if(readers == null) {
            throw new NullPointerException("The provided readers are null");
        }        
    }

    public void setFeatureMode(String featureMode)
    {
        this.featureMode = featureMode;
        nullCheckFeatureMode();
    }

    private void nullCheckFeatureMode()
    {
        if (this.learningMode == null) {
            throw new NullPointerException("The provided feature mode is null");
        }
    }

    public void setLearningMode(String learningMode)
    {
        this.learningMode = learningMode;
        nullCheckLearningMode();
    }

    private void nullCheckLearningMode()
    {
        if (this.learningMode == null) {
            throw new NullPointerException("The provided learning mode is null");
        }
    }

    public void addFeatureSet(TcFeatureSet featureSet)
    {
        sanityCheckFeatureSet(featureSet);
        this.featureSets.add(featureSet);
    }

    private void sanityCheckFeatureSet(TcFeatureSet featureSet)
    {
        if (featureSet == null) {
            throw new NullPointerException("The provided feature set is null");
        }
        if (featureSet.isEmpty()) {
            throw new IllegalStateException("The provided feature set contains no features");
        }
    }

    /**
     * Provides a reader that is added to the setup.
     * 
     * @param reader
     *            the reader
     * @param isTrain
     *            indicates if the reader provides information for training or testing data
     * @throws IllegalStateException
     *             if more than two reader instances are added
     */
    public void addReader(CollectionReaderDescription reader, boolean isTrain)
        throws IllegalStateException
    {
        if (reader == null) {
            throw new NullPointerException(
                    "Provided CollectionReaderDescription is null, please provide an initialized CollectionReaderDescription");
        }

        if (readers == null) {
            readers = new HashMap<>();
        }
        if (isTrain) {
            readers.put(DIM_READER_TRAIN, reader);
        }
        else {
            readers.put(DIM_READER_TEST, reader);
        }

        sanityCheckReaders();
    }

    private void sanityCheckReaders() throws IllegalStateException
    {
        if (readers.size() > 2) {
            throw new IllegalStateException(
                    "More than two readers have been added. Train-test experiments require two data readers, one for train, one for test. Cross-validation experiments require only one.");
        }

    }

}