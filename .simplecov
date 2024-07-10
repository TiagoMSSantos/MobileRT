require 'simplecov-cobertura'

SimpleCov.formatters = [SimpleCov::Formatter::CoberturaFormatter, SimpleCov::Formatter::HTMLFormatter]

SimpleCov.start do
    minimum_coverage 1
    add_filter "app/third_party/"
    add_filter "scripts/test/"
    add_filter "gradlew"
end
