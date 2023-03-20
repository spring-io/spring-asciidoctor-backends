#
# Copyright 2021 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# coding: utf-8

require "asciidoctor-pdf"

begin
  require "java"
rescue LoadError
end

class SpringPdfConverter < Asciidoctor::PDF::Converter

  register_for "spring-pdf"

  def convert_code node
    if (node.style != 'source')
      return super
    end
    add_dest_for_block node if node.id
    source_string = guard_indentation convert_listing_content(node)
    source_chunks = (XMLMarkupRx.match? source_string) ? (text_formatter.format source_string) : [text: source_string]
    adjusted_font_size = ((node.option? 'autofit') || (node.document.attr? 'autofit-option')) ? (theme_font_size_autofit source_chunks, :code) : nil
    theme_margin :block, :top
    caption_below = @theme.code_caption_end&.to_sym == :bottom
    arrange_block node do |extent|
#       caption_height = node.title? ? (layout_caption node, category: :code) : 0
      tare_first_page_content_stream do
        theme_fill_and_stroke_block :code, extent, background_color: nil, caption_node: caption_below ? nil : node
      end
      pad_box @theme.code_padding do
        theme_font :code do
          typeset_formatted_text source_chunks, (calc_line_metrics @theme.code_line_height || @theme.base_line_height),
              color: (@theme.code_font_color || @font_color),
              size: adjusted_font_size
        end
      end
    end
    stroke_horizontal_rule @theme.caption_border_bottom_color if node.title? && @theme.caption_border_bottom_color
    theme_margin :block, :bottom
  end

  def convert_listing_content(node)
    begin
      return Java::IoSpringAsciidoctorBackendCodetools::PdfListingContentConverters.content node
    rescue NameError
      return node.content
    end
  end


  alias convert_listing convert_code

end