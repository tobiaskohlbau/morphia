package dev.morphia.query;

import com.mongodb.client.model.geojson.Geometry;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.sofia.Sofia;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

/**
 * Defines various query filter operators
 *
 * @deprecated use {@link Filters} and {@link Filter} instead
 */
@Deprecated(since = "2.0", forRemoval = true)
public enum FilterOperator {

    WITHIN_CIRCLE("$center") {
        @Override
        public Filter apply(final String prop, final Object value) {
            // TODO
            throw new UnsupportedOperationException();
            //            return Filters.center(prop, value);
        }
    },

    WITHIN_CIRCLE_SPHERE("$centerSphere") {
        @Override
        public Filter apply(final String prop, final Object value) {
            // TODO
            throw new UnsupportedOperationException();
        }
    },

    WITHIN_BOX("$box") {
        @Override
        public Filter apply(final String prop, final Object value) {
            if (!(value instanceof Point[])) {
                throw new IllegalArgumentException(Sofia.illegalArgument(value.getClass().getCanonicalName(),
                    Point[].class.getCanonicalName()));
            }
            Point[] points = (Point[]) value;
            return Filters.box(prop, points[0], points[1]);
        }
    },

    EQUAL("$eq", "=", "==") {
        @Override
        public Filter apply(final String prop, final Object value) {
            return Filters.eq(prop, value);
        }
    },

    NOT_EQUAL("$ne", "!=", "<>") {
        @Override
        public Filter apply(final String prop, final Object value) {
            return Filters.ne(prop, value);
        }
    },

    GREATER_THAN("$gt", ">") {
        @Override
        public Filter apply(final String prop, final Object value) {
            return Filters.gt(prop, value);
        }
    },

    GREATER_THAN_OR_EQUAL("$gte", ">=") {
        @Override
        public Filter apply(final String prop, final Object value) {
            return Filters.gte(prop, value);
        }
    },

    LESS_THAN("$lt", "<") {
        @Override
        public Filter apply(final String prop, final Object value) {
            return Filters.lt(prop, value);
        }
    },

    LESS_THAN_OR_EQUAL("$lte", "<=") {
        @Override
        public Filter apply(final String prop, final Object value) {
            return Filters.lte(prop, value);
        }
    },

    EXISTS("$exists", "exists") {
        @Override
        public Filter apply(final String prop, final Object value) {
            Filter exists = Filters.exists(prop);
            if (Boolean.FALSE.equals(value)) {
                exists.not();
            }
            return exists;
        }
    },

    TYPE("$type", "type") {
        @Override
        public Filter apply(final String prop, final Object value) {
            return Filters.type(prop, (Type) value);
        }
    },

    NOT("$not") {
        @Override
        public Filter apply(final String prop, final Object value) {
            throw new UnsupportedOperationException(Sofia.translationNotCurrentlySupported());
        }
    },

    MOD("$mod", "mod") {
        @Override
        public Filter apply(final String prop, final Object value) {
            return Filters.mod(prop, value);
        }
    },

    SIZE("$size", "size") {
        @Override
        public Filter apply(final String prop, final Object value) {
            return Filters.size(prop, (Integer) value);
        }
    },

    IN("$in", "in") {
        @Override
        public Filter apply(final String prop, final Object value) {
            return Filters.in(prop, value);
        }
    },

    NOT_IN("$nin", "nin") {
        @Override
        public Filter apply(final String prop, final Object value) {
            return Filters.nin(prop, value);
        }
    },

    ALL("$all", "all") {
        @Override
        public Filter apply(final String prop, final Object value) {
            return Filters.all(prop, value);
        }
    },

    ELEMENT_MATCH("$elemMatch", "elem", "elemMatch") {
        @Override
        public Filter apply(final String prop, final Object value) {
            return Filters.elemMatch(prop, value);
        }
    },

    WHERE("$where") {
        @Override
        public Filter apply(final String prop, final Object value) {
            return Filters.where(value.toString());
        }
    },

    // GEO
    NEAR("$near", "near") {
        @Override
        public Filter apply(final String prop, final Object value) {
            return Filters.near(prop, (Point) convertToGeometry(value));
        }
    },

    NEAR_SPHERE("$nearSphere") {
        @Override
        public Filter apply(final String prop, final Object value) {
            return Filters.nearSphere(prop, (Point) convertToGeometry(value));
        }
    },

    GEO_NEAR("$geoNear", "geoNear") {
        @Override
        public Filter apply(final String prop, final Object value) {
            throw new UnsupportedOperationException("An aggregation operator called in a query context?");
        }
    },

    GEO_WITHIN("$geoWithin", "geoWithin") {
        @Override
        public Filter apply(final String prop, final Object value) {
            return Filters.geoWithin(prop, convertToGeometry(value));
        }
    },

    INTERSECTS("$geoIntersects", "geoIntersects") {
        @Override
        public Filter apply(final String prop, final Object value) {
            return Filters.geoIntersects(prop, convertToGeometry(value));
        }
    };

    private static Geometry convertToGeometry(final Object value) {
        Geometry converted;
        if (value instanceof double[]) {
            final double[] coordinates = (double[]) value;
            converted = new Point(new Position(coordinates[0], coordinates[1]));
        } else if (value instanceof Geometry) {
            converted = (Geometry) value;
        } else {
            throw new UnsupportedOperationException(Sofia.conversionNotSupported(value.getClass().getCanonicalName()));
        }

        return converted;
    }

    private final String value;
    private final List<String> filters;

    FilterOperator(final String val, final String... filterValues) {
        value = val;
        filters = Arrays.asList(filterValues);
    }

    /**
     * Creates a FilterOperator from a String
     *
     * @param operator the String to convert
     * @return the FilterOperator
     */
    public static FilterOperator fromString(final String operator) {
        final String filter = operator.trim().toLowerCase();
        for (FilterOperator filterOperator : FilterOperator.values()) {
            if (filterOperator.matches(filter)) {
                return filterOperator;
            }
        }
        throw new IllegalArgumentException(format("Unknown operator '%s'", operator));
    }

    /**
     * Converts a {@link FilterOperator} to a {@link Filter}
     * @param prop the document property name
     * @param value the value to apply to the filter
     * @return the new Filter
     * @morphia.internal
     */
    public abstract Filter apply(String prop, Object value);

    /**
     * Returns true if the given filter matches the filters on this FilterOperator
     *
     * @param filter the filter to check
     * @return true if the given filter matches the filters on this FilterOperator
     */
    public boolean matches(final String filter) {
        return filter != null && filters.contains(filter.trim().toLowerCase());
    }

    /**
     * @return the value of this FilterOperator
     */
    public String val() {
        return value;
    }
}
