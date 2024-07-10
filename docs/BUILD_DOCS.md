# MobileRT: Mobile Ray Tracing engine

## Build documentation locally

- Necessary to install Ruby 3.3.X.

```shell
gem install bundler -v 2.5.7;
bundle install && bundle update --bundler;
bundle exec jekyll build --source docs/ docs/_config.yml --trace;
bundle exec jekyll serve --source docs/ docs/_config.yml --trace;
```

baseurl: {{ site.github.baseurl }}  
repository_name: {{ site.github.repository_name }}  
source_branch: {{ site.github.source.branch }}  

Check <https://jekyll.github.io/github-metadata/site.github/> for more examples.
