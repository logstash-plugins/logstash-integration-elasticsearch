# encoding: utf-8
require "logstash/namespace"
require "logstash/outputs/base"

class LogStash::Outputs::ElasticSearch < LogStash::Outputs::Base
  declare_threadsafe!

  # XXX: Import the settings from the existing Elasticsearch output.
  config_name "elasticsearch2"

  config :index, :validate => :string, :default => "logstash-%{+YYYY.MM.dd}"
  config :hosts, :validate => :uri, :default => [::LogStash::Util::SafeURI.new("//127.0.0.1")], :list => true

  def initialize(config)

  end
end # class LogStash::Outputs::Elasticsearch
