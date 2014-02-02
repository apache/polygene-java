/*
 * Copyright 2014 Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.index.elasticsearch;

import java.math.BigDecimal;
import java.util.Map;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.joda.money.BigMoney;
import org.joda.money.BigMoneyProvider;
import org.qi4j.api.query.grammar.ComparisonSpecification;
import org.qi4j.api.query.grammar.ContainsAllSpecification;
import org.qi4j.api.query.grammar.ContainsSpecification;
import org.qi4j.api.query.grammar.EqSpecification;
import org.qi4j.api.query.grammar.GeSpecification;
import org.qi4j.api.query.grammar.GtSpecification;
import org.qi4j.api.query.grammar.LeSpecification;
import org.qi4j.api.query.grammar.LtSpecification;
import org.qi4j.api.query.grammar.NeSpecification;
import org.qi4j.api.query.grammar.Variable;

import static org.elasticsearch.index.query.FilterBuilders.andFilter;
import static org.elasticsearch.index.query.FilterBuilders.existsFilter;
import static org.elasticsearch.index.query.FilterBuilders.notFilter;
import static org.elasticsearch.index.query.FilterBuilders.orFilter;
import static org.elasticsearch.index.query.FilterBuilders.rangeFilter;
import static org.elasticsearch.index.query.FilterBuilders.termFilter;

/* package */ final class ElasticSearchFinderSupport
{

    /* package */ static Object resolveVariable( Object value, Map<String, Object> variables )
    {
        if( value == null )
        {
            return null;
        }
        if( value instanceof Variable )
        {
            Variable var = (Variable) value;
            Object realValue = variables.get( var.variableName() );
            if( realValue == null )
            {
                throw new IllegalArgumentException( "Variable " + var.variableName() + " not bound" );
            }
            return realValue;
        }
        return value;
    }

    /* package */ static interface ComplexTypeSupport
    {

        FilterBuilder comparison( ComparisonSpecification<?> spec, Map<String, Object> variables );

        FilterBuilder contains( ContainsSpecification<?> spec, Map<String, Object> variables );

        FilterBuilder containsAll( ContainsAllSpecification<?> spec, Map<String, Object> variables );

    }

    /* package */ static class MoneySupport
        implements ComplexTypeSupport
    {
        private static final String CURRENCY = ".currency";
        private static final String AMOUNT = ".amount";

        @Override
        public FilterBuilder comparison( ComparisonSpecification<?> spec, Map<String, Object> variables )
        {
            String name = spec.property().toString();
            String currencyTerm = name + CURRENCY;
            String amountTerm = name + AMOUNT;
            BigMoney money = ( (BigMoneyProvider) spec.value() ).toBigMoney();
            String currency = money.getCurrencyUnit().getCurrencyCode();
            BigDecimal amount = money.getAmount();
            if( spec instanceof EqSpecification )
            {
                return andFilter(
                    termFilter( currencyTerm, currency ),
                    termFilter( amountTerm, amount )
                );
            }
            else if( spec instanceof NeSpecification )
            {
                return andFilter(
                    existsFilter( name ),
                    orFilter( notFilter( termFilter( currencyTerm, currency ) ),
                              notFilter( termFilter( amountTerm, amount ) ) )
                );
            }
            else if( spec instanceof GeSpecification )
            {
                return andFilter(
                    termFilter( currencyTerm, currency ),
                    rangeFilter( amountTerm ).gte( amount )
                );
            }
            else if( spec instanceof GtSpecification )
            {
                return andFilter(
                    termFilter( currencyTerm, currency ),
                    rangeFilter( amountTerm ).gt( amount )
                );
            }
            else if( spec instanceof LeSpecification )
            {
                return andFilter(
                    termFilter( currencyTerm, currency ),
                    rangeFilter( amountTerm ).lte( amount )
                );
            }
            else if( spec instanceof LtSpecification )
            {
                return andFilter(
                    termFilter( currencyTerm, currency ),
                    rangeFilter( amountTerm ).lt( amount )
                );
            }
            else
            {
                throw new UnsupportedOperationException( "Query specification unsupported by Elastic Search "
                                                         + "(New Query API support missing?): "
                                                         + spec.getClass() + ": " + spec );
            }
        }

        @Override
        public FilterBuilder contains( ContainsSpecification<?> spec,
                                       Map<String, Object> variables )
        {
            String name = spec.collectionProperty().toString();
            BigMoney money = ( (BigMoneyProvider) spec.value() ).toBigMoney();
            String currency = money.getCurrencyUnit().getCurrencyCode();
            BigDecimal amount = money.getAmount();
            return andFilter(
                termFilter( name + CURRENCY, currency ),
                termFilter( name + AMOUNT, amount )
            );
        }

        @Override
        public FilterBuilder containsAll( ContainsAllSpecification<?> spec,
                                          Map<String, Object> variables )
        {
            String name = spec.collectionProperty().toString();
            AndFilterBuilder contAllFilter = new AndFilterBuilder();
            for( Object value : spec.containedValues() )
            {
                BigMoney money = ( (BigMoneyProvider) value ).toBigMoney();
                String currency = money.getCurrencyUnit().getCurrencyCode();
                BigDecimal amount = money.getAmount();
                contAllFilter.add( termFilter( name + CURRENCY, currency ) );
                contAllFilter.add( termFilter( name + AMOUNT, amount ) );
            }
            return contAllFilter;
        }

    }

    private ElasticSearchFinderSupport()
    {
    }

}
