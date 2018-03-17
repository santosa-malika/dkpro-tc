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
package org.dkpro.tc.api.features;

import java.util.Set;

import org.apache.uima.jcas.JCas;

import org.dkpro.tc.api.exception.TextClassificationException;

/**
 * Common signature for feature extractors which extract their features from a pair of documents.
 */
public interface PairFeatureExtractor
{
    /**
     * Extracts features from a pair of documents
     * 
     * @param view1
     *            First view to be processed
     * @param view2
     *            Second view to be processed
     * @throws TextClassificationException
     *             an Exception
     * @return a set of features generated by the extractor for the documents.
     * 
     */
    Set<Feature> extract(JCas view1, JCas view2) throws TextClassificationException;
}
